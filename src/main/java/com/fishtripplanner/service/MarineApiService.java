package com.fishtripplanner.service;

import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.dto.RecommendedFishingTime;
import com.fishtripplanner.dto.FishingApiType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
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

    // 갯바위/선상
    private void fetchFishingIndex(MarineInfoResponseDto result, String code, String date, String url) {
        String fullUrl = url + "?areaCode=" + code + "&date=" + date;
        Map<?, ?> response = restTemplate.getForObject(fullUrl, Map.class);

        // 응답 구조에 따라 "selectFishing" 사용
        var list = (List<Map<String, Object>>) response.get("selectFishing");
        if (list == null) return;

        for (Map<String, Object> item : list) {
            result.addRecommendedTime(RecommendedFishingTime.builder()
                    .date((String) item.get("date"))
                    .time((String) item.get("pred_type")) // "pred_type"이 시간대(오전/오후/일)
                    .fishName((String) item.get("fishName"))
                    .waterTemp((String) item.get("waterTemp"))
                    .waveHeight((String) item.get("waveHeight"))
                    .airTemp((String) item.get("airTemp"))
                    .currentSpeed((String) item.get("currentSpeed"))
                    .tide((String) item.get("tideTime"))
                    .fishingIndex((String) item.get("fishingIndex"))
                    .fishingScore((String) item.get("fishingIndex"))
                    .build());
        }
    }

    // 기타 생활낚시
    private void fetchEtcFishing(MarineInfoResponseDto result, String code) {
        String url = "https://www.khoa.go.kr/khoa/lifeforecast/getEtcFishing.do?areaCode=" + code;
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);

        // selectFishing이 배열로 온다
        var list = (List<Map<String, Object>>) response.get("selectFishing");
        if (list == null) return;

        for (Map<String, Object> item : list) {
            result.addRecommendedTime(RecommendedFishingTime.builder()
                    .date((String) item.get("date"))
                    .time((String) item.get("pred_type"))
                    .fishName((String) item.get("fishName"))
                    .waterTemp((String) item.get("waterTemp"))
                    .waveHeight((String) item.get("waveHeight"))
                    .airTemp((String) item.get("airTemp"))
                    .fishingIndex((String) item.get("fishingIndex"))
                    .fishingScore((String) item.get("fishingIndex"))
                    .build());
        }
    }

    // 해양여행/일반예보
    private void fetchSeaTravel(MarineInfoResponseDto result, String code, String date) {
        String url = "https://www.khoa.go.kr/khoa/lifeforecast/getSeaTravelData.do?fcstArea=" + code + "&date=" + date;
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        var list = (List<Map<String, Object>>) response.get("tableList");
        if (list == null) return;

        for (Map<String, Object> item : list) {
            result.addRecommendedTime(RecommendedFishingTime.builder()
                    .date((String) item.get("date"))
                    .time((String) item.get("pred_type")) // key가 다르면 "time" 또는 "predType"
                    .fishName((String) item.get("fishName"))
                    .waterTemp((String) item.get("waterTemp"))
                    .waveHeight((String) item.get("waveHeight"))
                    .airTemp((String) item.get("airTemp"))
                    .currentSpeed((String) item.get("currentSpeed"))
                    .tide((String) item.get("tideTime"))
                    .fishingIndex((String) item.get("fishingIndex"))
                    .area((String) item.get("area"))
                    .location((String) item.get("location"))
                    .windSpeed((String) item.get("windSpeed"))
                    .weather((String) item.get("weather"))
                    .fishingScore((String) item.get("fishingIndex"))
                    .build());
        }
    }


}
