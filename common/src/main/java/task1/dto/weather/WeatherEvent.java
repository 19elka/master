package task1.dto.weather;

import java.time.Instant;

public record WeatherEvent(
        String city,
        double temperature,
        String condition,
        Instant timestamp
) {
}
