package task1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiResponseDto(
        @JsonProperty("current_weather")
        WeatherCurrentResponseDto weatherCurrentResponseDto
) { }