package task1.dto;

public record WeatherCurrentResponseDto(
        double temperature,
        double windspeed,
        int weathercode,
        String time
) {
}