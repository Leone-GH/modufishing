package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.dto.RecommendedFishingTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripMarineInfoService {

    private static final String BASE_URL = "https://www.khoa.go.kr/khoa/lifeforecast/getSeaTravelData.do";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MarineInfoResponseDto getMarineInfo(double lat, double lon, String date, String arrivalTime) {
        AreaCodes nearest = AreaCodesUtil.findNearest(lat, lon, AreaCodes.AreaType.TRIP)
                .orElseThrow(() -> new IllegalArgumentException("TRIP 관측소 없음"));
        String areaCode = nearest.getCode();
        String url = BASE_URL + "?fcstArea=" + areaCode + "&date=" + date;
        log.info("TRIP API 호출: {}", url);

        MarineInfoResponseDto result = new MarineInfoResponseDto();
        try {
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode tableList = root.path("tableList");

            List<RecommendedFishingTime> recommendedTimes = new ArrayList<>();
            for (JsonNode node : tableList) {
                RecommendedFishingTime time = RecommendedFishingTime.builder()
                        .date(node.path("predDate").asText())
                        .time(node.path("predType").asText())
                        .fishType("해양예보")
                        .waterTemp(node.path("avgWaterTemp").asText())
                        .waveHeight(node.path("avgWaveHeight").asText())
                        .airTemp(node.path("avgAirTemp").asText())
                        .fishingScore(0) // 필요시 값 매핑
                        .fishingIndex(node.path("totalScoreStr").asText())
                        .build();
                recommendedTimes.add(time);
            }
            result.setRecommendedTimes(recommendedTimes);

        } catch (Exception e) {
            log.error("TRIP 해양예보 파싱 오류", e);
        }

        return result;
    }
}
