package com.ddbb.controller.aidashboard;

import com.ddbb.service.aidashboard.WeatherService;
import com.ddbb.service.aidashboard.WeatherService.WeatherSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final WeatherService weatherService;

    public DashboardController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/insights")
    public Map<String, Object> getDashboardInsights() {
        WeatherSummary weather = weatherService.fetchSeoulWeather();

        String temperatureText = weather.temperature() != null
                ? String.format("서울 현재 %.1f°C", weather.temperature())
                : "서울 현재 기온";

        String conditionPhrase = weather.description() != null
                ? formatConditionForSentence(weather.description())
                : "편안한 날씨";

        String mood = weather.success()
                ? String.format("%s, %s라 소금빵이 특히 인기입니다.", temperatureText, conditionPhrase)
                : "날씨 정보를 불러오지 못했지만, 산뜻한 소금빵으로 매장을 밝혀보세요.";

        String salesBrief = "오전 시간대 소금빵이 강세를 보이며, 점심 이후 크로와상이 뒤를 이었습니다.";

        String stockInsight = "단팥빵 재고가 부족해 오전에 품절될 가능성이 있습니다.";

        String strategy = buildStrategySuggestion(weather, conditionPhrase, temperatureText);

        return Map.of(
                "mood", mood,
                "brief", salesBrief,
                "insight", stockInsight,
                "strategy", strategy
        );
    }

    private String formatConditionForSentence(String condition) {
        return switch (condition) {
            case "맑음" -> "맑은 날씨";
            case "부분적으로 흐림" -> "구름이 드문 날씨";
            case "안개" -> "안개 낀 아침";
            case "이슬비" -> "부슬비 내리는 날씨";
            case "얼어붙는 이슬비" -> "얼어붙는 이슬비가 내리는 날씨";
            case "비" -> "비 오는 날씨";
            case "얼어붙는 비" -> "얼어붙는 비가 내리는 날씨";
            case "눈" -> "눈 내리는 날씨";
            case "진눈깨비" -> "진눈깨비가 내리는 날씨";
            case "소나기" -> "소나기가 지나는 날씨";
            case "눈 소나기" -> "눈 소나기가 예보된 날씨";
            case "천둥번개" -> "천둥번개가 예보된 날씨";
            case "천둥번개와 우박" -> "천둥번개와 우박이 예보된 날씨";
            default -> condition + " 분위기";
        };
    }

    private String buildStrategySuggestion(WeatherSummary weather, String conditionPhrase, String temperatureText) {
        if (!weather.success()) {
            return "단팥빵 2+1 이벤트로 재고를 효율적으로 소진해보세요.";
        }

        boolean hasCondition = weather.description() != null;
        boolean hasTemperature = weather.temperature() != null;

        if (hasCondition && hasTemperature) {
            return String.format("%s와 %s를 고려해 단팥빵 2+1 이벤트로 재고를 효율적으로 소진해보세요.",
                    conditionPhrase,
                    String.format("%.1f°C 기온", weather.temperature()));
        }

        if (hasCondition) {
            return String.format("%s에 맞춰 단팥빵 2+1 이벤트로 재고를 효율적으로 소진해보세요.", conditionPhrase);
        }

        if (hasTemperature) {
            return String.format("%s에 맞춰 단팥빵 2+1 이벤트로 재고를 효율적으로 소진해보세요.", temperatureText);
        }

        return "단팥빵 2+1 이벤트로 재고를 효율적으로 소진해보세요.";
    }
}
