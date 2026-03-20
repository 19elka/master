package task1.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import task1.dto.weather.WeatherEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TOPIC = "weather-updates";

    public void sendWeatherEvent(WeatherEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, message);
            log.info("Weather event sent: {}°C in {}", event.temperature(), event.city());
        } catch (Exception e) {
            log.error("Failed to send weather event: {}", e.getMessage());
        }
    }
}