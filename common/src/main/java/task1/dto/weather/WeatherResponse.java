package task1.dto.weather;

public record WeatherResponse(
        String city,
        double temperature,
        String condition,
        long timestamp
) {
}
