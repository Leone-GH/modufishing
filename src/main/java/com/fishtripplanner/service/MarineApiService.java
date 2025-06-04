package com.fishtripplanner.service;

import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.dto.RecommendedFishingTime;
import com.fishtripplanner.dto.FishingApiType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarineApiService {

    private final RestTemplate restTemplate;

    public MarineInfoResponseDto fetchData(String type, String code, String date) {
        MarineInfoResponseDto result = new MarineInfoResponseDto();
        String requestDate = (date != null) ? date : LocalDate.now().toString().replace("-", "");
        FishingApiType apiType = FishingApiType.from(type);

        switch (apiType) {
            case ROCK -> fetchFishingIndex(result, code, requestDate, "https://www.khoa.go.kr/khoa/lifeforecast/getFishing.do");
            case BOAT -> fetchFishingIndex(result, code, requestDate, "https://www.khoa.go.kr/khoa/lifeforecast/getFishing2.do");
            case ETC -> fetchEtcFishing(result, code);
            case TRIP -> fetchSeaTravel(result, code, requestDate);
        }

        return result;
    }

    private void fetchFishingIndex(MarineInfoResponseDto result, String code, String date, String url) {
        String fullUrl = url + "?areaCode=" + code + "&date=" + date;
        Map<?, ?> response = restTemplate.getForObject(fullUrl, Map.class);
        var list = (java.util.List<Map<String, Object>>) response.get("resultList");

        for (Map<String, Object> item : list) {
            result.addRecommendedTime(RecommendedFishingTime.builder()
                    .date((String) item.get("date"))
                    .time((String) item.get("time"))
                    .fishType((String) item.get("fishName"))
                    .waterTemp((String) item.get("waterTemp"))
                    .waveHeight((String) item.get("waveHeight"))
                    .airTemp((String) item.get("airTemp"))
                    .currentSpeed((String) item.get("currentSpeed"))
                    .tide((String) item.get("tideTime"))
                    .fishingIndex((String) item.get("fishingIndex"))
                    .fishingScore(parseScore((String) item.get("fishingIndex")))
                    .build());
        }
    }

    private void fetchEtcFishing(MarineInfoResponseDto result, String code) {
        String url = "https://www.khoa.go.kr/khoa/lifeforecast/getEtcFishing.do?areaCode=" + code;
        Map<String, Object> item = restTemplate.getForObject(url, Map.class);
        result.addRecommendedTime(RecommendedFishingTime.builder()
                .date(LocalDate.now().toString())
                .time("일")
                .fishType((String) item.get("fishName"))
                .waterTemp((String) item.get("waterTemp"))
                .waveHeight((String) item.get("waveHeight"))
                .airTemp((String) item.get("airTemp"))
                .fishingIndex("정보")
                .fishingScore(50)
                .build());
    }

    private void fetchSeaTravel(MarineInfoResponseDto result, String code, String date) {
        String url = "https://www.khoa.go.kr/khoa/lifeforecast/getSeaTravelData.do?fcstArea=" + code + "&date=" + date;
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        var list = (java.util.List<Map<String, Object>>) response.get("tableList");

        for (Map<String, Object> item : list) {
            result.addRecommendedTime(RecommendedFishingTime.builder()
                    .date((String) item.get("predDate"))
                    .time((String) item.get("predType"))
                    .fishType("해양여행")
                    .waterTemp((String) item.get("avgWaterTemp"))
                    .waveHeight((String) item.get("avgWaveHeight"))
                    .airTemp((String) item.get("avgAirTemp"))
                    .fishingIndex((String) item.get("totalScoreStr"))
                    .fishingScore(parseScore((String) item.get("totalScoreStr")))
                    .build());
        }
    }

    private int parseScore(String index) {
        if (index == null) return 0;
        return switch (index) {
            case "매우좋음" -> 100;
            case "좋음" -> 80;
            case "보통" -> 60;
            case "나쁨" -> 40;
            case "매우나쁨" -> 20;
            default -> 50;
        };
    }
}
