package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishtripplanner.dto.FishingIndexDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public List<FishingIndexDto> fetchFishingIndex(double lat, double lon, String date, AreaCodes.AreaType type) {
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

    private List<FishingIndexDto> parseResponse(String json) {
        List<FishingIndexDto> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode list = root.path("selectFishing");
            for (JsonNode node : list) {
                result.add(FishingIndexDto.builder()
                        .dateStr(node.path("date").asText())
                        .timeStr(node.path("pred_type").asText())
                        .fishName(node.path("fishName").asText())
                        .fishingIndex(node.path("fishingIndex").asText())
                        .waveHeight(node.path("waveHeight").asText())
                        .currentSpeed(node.path("currentSpeed").asText())
                        .waterTemp(node.path("waterTemp").asText())
                        .airTemp(node.path("airTemp").asText())
                        .tide(node.path("tideTime").asText())
                        .fishingScore(0) // 필요시 파싱 추가
                        .build());
            }
        } catch (Exception e) {
            log.error("JSON 파싱 오류", e);
        }
        return result;
    }
}
