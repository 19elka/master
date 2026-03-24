package task1.dto.weather;

public record WeatherEvent(
        String city,
        double temperature,
        String condition,
        long timestamp
) {
}
