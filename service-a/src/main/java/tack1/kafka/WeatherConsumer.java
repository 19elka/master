package task1.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import task1.dto.weather.WeatherEvent;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("service-a")
public class WeatherConsumer {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "weather", groupId = "weather-consumer")
    public void consumerWeatherEvent(String message) {
        try {
            log.info("Received weather event from Kafka: {}", message);
            WeatherEvent event = objectMapper.readValue(message, WeatherEvent.class);

            String key = "weather:" + event.city();
            String value = objectMapper.writeValueAsString(event);
            redisTemplate.opsForValue().set(key, value);
            log.info("Saved weather for {} to Redis", event.city());
        } catch (Exception e) {
            log.error("Failed to process weather event: {}", e.getMessage());
        }
    }
}
