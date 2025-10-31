package com.ddbb.controller.aidashboard;

import com.ddbb.service.aidashboard.WeatherService;
import com.ddbb.service.aidashboard.WeatherService.WeatherSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/today")
    public Map<String, Object> getTodayWeather() {
        WeatherSummary summary = weatherService.fetchSeoulWeather();
        if (summary.success()) {
            return Map.of(
                    "weather", summary.summary(),
                    "condition", summary.description(),
                    "temperature", summary.temperature()
            );
        }
        return Map.of("weather", summary.summary());
    }
}
