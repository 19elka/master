package task1.dto.weather;

import java.time.Instant;

public record WeatherResponse(
        String city,
        double temperature,
        String condition,
        Instant timestamp
) {
}
