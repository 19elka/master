package task1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherCurrentResponseDto(
        double temperature,
        double windspeed,
        int weathercode,
        String time
) {
}