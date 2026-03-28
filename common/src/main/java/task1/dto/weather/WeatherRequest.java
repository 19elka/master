package task1.dto.weather;

import jakarta.validation.constraints.NotBlank;

public record WeatherRequest(
        @NotBlank
        String city
) {
}