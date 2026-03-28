package task1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import task1.dto.weather.WeatherResponse;
import task1.grpc.WeatherProto;
import task1.grpc.WeatherServiceGrpc;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    @GrpcClient("weather-service-b")
    private WeatherServiceGrpc.WeatherServiceBlockingStub weatherStub;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Async
    public void updateWeatherAsync(String city) {
        if (city == null || city.trim().isEmpty()) {
            log.warn("Invalid city name: {}", city);
            return;
        }

        city = city.trim();
        log.info("Updating weather for city: {}", city);

        try {
            WeatherProto.WeatherRequest request = WeatherProto.WeatherRequest.newBuilder()
                    .setCity(city)
                    .build();

            weatherStub.getWeather(request);
            log.info("gRPC weather update triggered for {}", city);
        } catch (Exception e) {
            log.error("gRPC failed for {}, returning cached data", city, e);
        }
    }

    public WeatherResponse getWeather(String city) {
        if (city == null || city.trim().isEmpty()) {
            log.warn("Invalid city name: {}", city);
            return new WeatherResponse("invalid", 0.0, "error", Instant.now());
        }

        city = city.trim();
        String key = "weather:" + city;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            log.warn("Weather data not found in Redis for city: {}", city);
            return new WeatherResponse(city, 0.0, "not_found", Instant.now());
        }

        try {
            WeatherResponse response = objectMapper.readValue(value, WeatherResponse.class);
            log.info("Read weather from Redis for {}: {}°C, {}", city, response.temperature(), response.condition());
            return response;
        } catch (Exception e) {
            log.error("Failed to parse weather from Redis: {}", e.getMessage());
            return new WeatherResponse(city, 0.0, "error", Instant.now());
        }
    }
}
