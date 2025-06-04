package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.dto.RecommendedFishingTime;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.fishtripplanner.api.khoa.AreaCodesUtil.findNearest;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifeFishingIndexService {

    private static final String BASE_URL = "https://www.khoa.go.kr/khoa/lifeforecast/getFishing.do";
    private static final String BOAT_URL = "https://www.khoa.go.kr/khoa/lifeforecast/getFishing2.do";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 갯바위(rock) 및 선상(boat) 타입별로 AreaCodes enum에서 가장 가까운 코드 사용
    public List<FishingIndex> fetchFishingIndex(double lat, double lon, String date, AreaCodes.AreaType type) {
        AreaCodes area = findNearest(lat, lon, type).orElseThrow(() -> new IllegalArgumentException("해당 타입의 지역 없음"));
        String areaCode = area.getCode();

        String url;
        if (type == AreaCodes.AreaType.ROCK) {
            url = String.format("%s?areaCode=%s&date=%s", BASE_URL, areaCode, date);
        } else if (type == AreaCodes.AreaType.BOAT) {
            url = String.format("%s?areaCode=%s&date=%s", BOAT_URL, areaCode, date);
        } else {
            throw new IllegalArgumentException("지원하지 않는 타입: " + type);
        }

        log.info("생활낚시지수 API 호출: {}", url);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("생활낚시지수 API 호출 오류", e);
            return Collections.emptyList();
        }
    }

    private List<FishingIndex> parseResponse(String json) {
        List<FishingIndex> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode tableList = root.path("tableList");
            for (JsonNode node : tableList) {
                FishingIndex index = FishingIndex.builder()
                        .dateStr(node.path("date").asText())
                        .timeStr(node.path("time").asText())
                        .fishName(node.path("fishType").asText())
                        .fishingIndex(node.path("fishingIndex").asText())
                        .fishingScore(parseScore(node.path("fishingScore").asText()))
                        .waveHeight(node.path("waveHeight").asText())
                        .waterTemp(node.path("waterTemp").asText())
                        .airTemp(node.path("airTemp").asText())
                        .build();
                list.add(index);
            }
        } catch (Exception e) {
            log.error("파싱 오류", e);
        }
        return list;
    }

    private double parseScore(String score) {
        try {
            return Double.parseDouble(score);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // 필요 시: 추천 시간 추출 등은 별도 메서드로
    public Optional<FishingIndex> recommendBestTime(List<FishingIndex> list, String fishType, String date) {
        return list.stream()
                .filter(f -> f.getFishName().equals(fishType))
                .filter(f -> f.getDateStr().startsWith(date))
                .max(Comparator.comparingDouble(FishingIndex::getFishingScore));
    }

    @Data
    @Builder
    public static class FishingIndex {
        private String dateStr;
        private String timeStr;
        private String fishName;
        private String fishingIndex;
        private double fishingScore;
        private String waveHeight;
        private String waterTemp;
        private String airTemp;
    }
}
