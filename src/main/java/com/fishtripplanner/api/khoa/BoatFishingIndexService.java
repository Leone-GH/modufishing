package com.fishtripplanner.api.khoa;

import com.fishtripplanner.dto.FishingIndexDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoatFishingIndexService {

    private static final String API_URL = "https://www.khoa.go.kr/khoa/lifeforecast/getFishing2.do";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<FishingIndexDto> fetchBoatFishingIndex(String areaCode, String date) {
        try {
            String url = API_URL + "?areaCode=" + areaCode + "&date=" + date;
            String json = restTemplate.getForObject(url, String.class);
            return parseResponse(json);
        } catch (Exception e) {
            log.error("선상 낚시지수 호출 오류", e);
            return Collections.emptyList();
        }
    }

    private List<FishingIndexDto> parseResponse(String json) {
        List<FishingIndexDto> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode list = root.path("result").path("data");
            for (JsonNode node : list) {
                result.add(FishingIndexDto.builder()
                        .dateStr(node.path("date").asText())
                        .timeStr(node.path("time").asText())
                        .fishName(node.path("fishType").asText())
                        .fishingIndex(node.path("fishingIndex").asText())
                        .fishingScore(parseDouble(node.path("fishingScore").asText()))
                        .waveHeight(node.path("waveHeight").asText())
                        .currentSpeed(node.path("currentSpeed").asText())
                        .waterTemp(node.path("waterTemp").asText())
                        .airTemp(node.path("airTemp").asText())
                        .tide(node.path("tide").asText())
                        .build());
            }
        } catch (Exception e) {
            log.error("선상 JSON 파싱 오류", e);
        }
        return result;
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }
}

