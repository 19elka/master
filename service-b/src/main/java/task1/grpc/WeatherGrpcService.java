package task1.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import task1.config.WeatherCodeProperties;
import task1.dto.WeatherApiResponseDto;
import task1.dto.WeatherCurrentResponseDto;
import task1.dto.weather.WeatherEvent;
import task1.kafka.WeatherProducer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@GrpcService
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final WebClient webClient;
    private final WeatherProducer weatherProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CityCoordinates> coordinatesCache = new ConcurrentHashMap<>();
    private final WeatherCodeProperties weatherCodeProperties;

    @Value("${weather.api.geo-url}")
    private String geoBaseUrl;

    @Value("${weather.api.forecast-url}")
    private String forecastBaseUrl;

    @Value("${weather.api.timezone}")
    private String timezone;

    @Value("${weather.api.temperature-unit}")
    private String temperatureUnit;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public WeatherGrpcService(WeatherProducer weatherProducer, WebClient.Builder builder, WeatherCodeProperties weatherCodeProperties) {
        this.weatherProducer = weatherProducer;
        this.webClient = builder.build();
        this.weatherCodeProperties = weatherCodeProperties;
    }

    private void processWeatherUpdate(String city) {
        try {
            CityCoordinates coordinates = getCityCoordinates(city);
            log.debug("Coordinates for {}: lat={}, lon={}", city, coordinates.latitude(), coordinates.longitude());

            String weatherUrl = UriComponentsBuilder.fromHttpUrl(forecastBaseUrl)
                    .queryParam("latitude", coordinates.latitude())
                    .queryParam("longitude", coordinates.longitude())
                    .queryParam("current_weather", "true")
                    .queryParam("timezone", timezone)
                    .queryParam("temperature_unit", temperatureUnit)
                    .build()
                    .toUriString();

            String weatherJson = webClient.get()
                    .uri(weatherUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));

            if (weatherJson == null || weatherJson.isBlank()) {
                throw new IllegalStateException("Empty JSON from Open-Meteo");
            }

            WeatherApiResponseDto apiResponse =
                    objectMapper.readValue(weatherJson, WeatherApiResponseDto.class);

            WeatherCurrentResponseDto current = apiResponse.weatherCurrentResponseDto();

            if (current == null) {
                throw new IllegalStateException("No current_weather data in response");
            }

            double temperature = current.temperature();
            int weatherCode = current.weathercode();
            String condition = mapWeatherCode(weatherCode);
            Instant timestamp = Instant.now();

            log.info("Parsed Open-Meteo weather for {}: {}°C, code={}, condition={}",
                    city, temperature, weatherCode, condition);

            WeatherEvent event = new WeatherEvent(city, temperature, condition, timestamp);
            weatherProducer.sendWeatherEvent(event);
        } catch (Exception e) {
            log.error("Failed to get/parse weather for {} from Open-Meteo: {}", city, e.getMessage());
        }
    }

    @Override
    public void startWeatherUpdate(WeatherProto.WeatherRequest request,
                                   StreamObserver<WeatherProto.WeatherStatusResponse> responseObserver) {
        String city = request.getCity();
        log.info("Received StartWeatherUpdate request for city: {}", city);

        executorService.submit(() -> processWeatherUpdate(city));

        WeatherProto.WeatherStatusResponse response =
                WeatherProto.WeatherStatusResponse.newBuilder()
                        .setCity(city)
                        .setStatus("STARTED")
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getWeather(WeatherProto.WeatherRequest request,
                           StreamObserver<WeatherProto.WeatherResponse> responseObserver) {
        responseObserver.onError(
                io.grpc.Status.UNIMPLEMENTED
                        .withDescription("Use StartWeatherUpdate instead")
                        .asRuntimeException()
        );
    }

    private CityCoordinates getCityCoordinates(String city) throws Exception {
        String key = city.toLowerCase();

        if (coordinatesCache.containsKey(key)) {
            log.debug("Using cached coordinates for: {}", city);
            return coordinatesCache.get(key);
        }

        String geoUrl = UriComponentsBuilder
                .fromHttpUrl(geoBaseUrl)
                .queryParam("name", URLEncoder.encode(city, StandardCharsets.UTF_8))
                .queryParam("count", 1)
                .queryParam("format", "json")
                .build()
                .toUriString();

        String geoJson = webClient.get()
                .uri(geoUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(5));

        if (geoJson == null || geoJson.isBlank()) {
            throw new IllegalStateException("Empty response from Geocoding API");
        }

        JsonNode root = objectMapper.readTree(geoJson);
        JsonNode results = root.path("results");

        if (results.isEmpty() || results.isMissingNode()) {
            throw new IllegalStateException("City not found: " + city);
        }

        JsonNode first = results.get(0);
        double latitude = first.path("latitude").asDouble();
        double longitude = first.path("longitude").asDouble();

        if (latitude == 0.0 && longitude == 0.0) {
            throw new IllegalStateException("Invalid coordinates for city: " + city);
        }

        CityCoordinates coordinates = new CityCoordinates(latitude, longitude);
        coordinatesCache.put(key, coordinates);

        log.info("Resolved coordinates for {}: lat={}, lon={}", city, latitude, longitude);
        return coordinates;
    }

    private String mapWeatherCode(int code) {
        String condition = weatherCodeProperties.codes().get(code);

        if (condition == null) {
            log.warn("Unknown weather code from API: {}", code);
            return "unknown";
        }
        return condition;
    }

    private record CityCoordinates(double latitude, double longitude) {
    }
}