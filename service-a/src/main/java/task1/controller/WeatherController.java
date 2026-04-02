package task1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import task1.dto.WeatherUpdateResponse;
import task1.dto.weather.WeatherResponse;
import task1.service.WeatherService;

@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @PostMapping("/{city}")
    public WeatherUpdateResponse updateWeather(@PathVariable String city, @RequestHeader(value = "X-From-Nginx", required = false) String fromNginx) {
        return weatherService.updateWeather(city);
    }

    @GetMapping
    public WeatherResponse getWeather(@RequestParam String city, @RequestHeader(value = "X-From-Nginx", required = false) String fromNginx) {
        return weatherService.getWeather(city);
    }
}
