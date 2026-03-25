package task1.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import task1.dto.weather.WeatherEvent;
import task1.kafka.WeatherProducer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@GrpcService
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final WebClient webClient;
    private final WeatherProducer weatherProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CityCoordinates> coordinatesCache = new ConcurrentHashMap<>();

    public WeatherGrpcService(WeatherProducer weatherProducer, WebClient.Builder builder) {
        this.weatherProducer = weatherProducer;
        this.webClient = builder.build();
    }

    @Override
    public void getWeather(WeatherProto.WeatherRequest request,
                           StreamObserver<WeatherProto.WeatherResponse> responseObserver) {
        String city = request.getCity();
        log.info("Received gRPC request for weather in: {}", city);

        try {
            CityCoordinates coordinates = getCityCoordinates(city);
            log.debug("Coordinates for {}: lat={}, lon={}", city, coordinates.latitude(), coordinates.longitude());

            String weatherUrl = UriComponentsBuilder.fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                    .queryParam("latitude", coordinates.latitude())
                    .queryParam("longitude", coordinates.longitude())
                    .queryParam("current_weather", "true")
                    .queryParam("timezone", "auto")
                    .build()
                    .toUriString();

            String weatherJson = webClient.get()
                    .uri(weatherUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));

            if (weatherJson == null) {
                throw new IllegalStateException("Empty JSON from Open-Meteo");
            }

            JsonNode root = objectMapper.readTree(weatherJson);
            JsonNode current = root.path("current_weather");

            if (current.isMissingNode()) {
                throw new IllegalStateException("No current_weather data in response");
            }

            double temperature = current.path("temperature").asDouble(0.0);
            int weatherCode = current.path("weathercode").asInt(-1);
            String condition = mapWeatherCode(weatherCode);
            long timestamp = System.currentTimeMillis();

            log.info("Parsed Open-Meteo weather for {}: {}°C, code={}, condition={}",
                    city, temperature, weatherCode, condition);

            WeatherEvent event = new WeatherEvent(city, temperature, condition, timestamp);
            weatherProducer.sendWeatherEvent(event);

            WeatherProto.WeatherResponse response = WeatherProto.WeatherResponse.newBuilder()
                    .setCity(city)
                    .setTemperature(temperature)
                    .setCondition(condition)
                    .setTimestamp(timestamp)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed weather for {}", city);

        } catch (Exception e) {
            log.error("Failed to get/parse weather for {} from Open-Meteo: {}", city, e.getMessage());

            WeatherProto.WeatherResponse fallback = WeatherProto.WeatherResponse.newBuilder()
                    .setCity(city)
                    .setTemperature(0.0)
                    .setCondition("unavailable")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(fallback);
            responseObserver.onCompleted();
        }
    }

    private CityCoordinates getCityCoordinates(String city) throws Exception {
        String key = city.toLowerCase();

        if (coordinatesCache.containsKey(key)) {
            log.debug("Using cached coordinates for: {}", city);
            return coordinatesCache.get(key);
        }

        String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&count=1&format=json";

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
        return switch (code) {
            case 0 -> "clear";
            case 1, 2 -> "partly_cloudy";
            case 3 -> "cloudy";
            case 45, 48 -> "fog";
            case 51, 53, 55 -> "drizzle";
            case 56, 57 -> "freezing_drizzle";
            case 61, 63, 65 -> "rain";
            case 66, 67 -> "freezing_rain";
            case 71, 73, 75 -> "snow";
            case 77 -> "snow_grains";
            case 80, 81, 82 -> "rain_showers";
            case 85, 86 -> "snow_showers";
            case 95 -> "thunderstorm";
            case 96, 99 -> "thunderstorm_with_hail";
            default -> "unknown";
        };
    }

    private record CityCoordinates(double latitude, double longitude) {
    }
}