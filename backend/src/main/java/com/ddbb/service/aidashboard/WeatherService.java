package com.ddbb.service.aidashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final String API_URL = "https://api.open-meteo.com/v1/forecast"
            + "?latitude=37.5665"
            + "&longitude=126.9780"
            + "&current_weather=true"
            + "&timezone=Asia/Seoul";

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherSummary fetchSeoulWeather() {
        try {
            Map<?, ?> response = restTemplate.getForObject(API_URL, Map.class);
            if (response == null || !response.containsKey("current_weather")) {
                return WeatherSummary.error("날씨 데이터를 불러올 수 없습니다.");
            }

            Object currentObj = response.get("current_weather");
            if (!(currentObj instanceof Map<?, ?> current)) {
                return WeatherSummary.error("날씨 데이터를 불러올 수 없습니다.");
            }

            Object tempObj = current.get("temperature");
            Object weatherCodeObj = current.get("weathercode");

            if (!(tempObj instanceof Number temperatureNumber) || !(weatherCodeObj instanceof Number weatherCodeNumber)) {
                return WeatherSummary.error("날씨 데이터를 불러올 수 없습니다.");
            }

            double temperature = temperatureNumber.doubleValue();
            int weatherCode = weatherCodeNumber.intValue();

            String description = translateWeatherCode(weatherCode);
            String summary = String.format("서울 현재 %.1f°C, %s", temperature, description);

            return WeatherSummary.success(summary, description, temperature);

        } catch (Exception e) {
            log.warn("날씨 API 호출 실패", e);
            return WeatherSummary.error("날씨 API 호출 오류: " + e.getMessage());
        }
    }

    private String translateWeatherCode(int code) {
        return switch (code) {
            case 0 -> "맑음";
            case 1, 2, 3 -> "부분적으로 흐림";
            case 45, 48 -> "안개";
            case 51, 53, 55 -> "이슬비";
            case 56, 57 -> "얼어붙는 이슬비";
            case 61, 63, 65 -> "비";
            case 66, 67 -> "얼어붙는 비";
            case 71, 73, 75 -> "눈";
            case 77 -> "진눈깨비";
            case 80, 81, 82 -> "소나기";
            case 85, 86 -> "눈 소나기";
            case 95 -> "천둥번개";
            case 96, 99 -> "천둥번개와 우박";
            default -> "변덕스러운 날씨";
        };
    }

    public record WeatherSummary(boolean success, String summary, String description, Double temperature) {
        public static WeatherSummary success(String summary, String description, Double temperature) {
            return new WeatherSummary(true, summary, description, temperature);
        }

        public static WeatherSummary error(String message) {
            return new WeatherSummary(false, message, null, null);
        }
    }
}
