package task1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "weather")
public record WeatherCodeProperties(
        Map<Integer, String> codes
) {
}