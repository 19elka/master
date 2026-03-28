package task1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherApiResponseDto(
        @JsonProperty("current_weather")
        WeatherCurrentResponseDto weatherCurrentResponseDto
) { }