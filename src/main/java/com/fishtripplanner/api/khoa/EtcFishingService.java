package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishtripplanner.dto.EtcFishingInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EtcFishingService {

    private static final String API_URL = "https://www.khoa.go.kr/khoa/lifeforecast/getEtcFishing.do";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EtcFishingInfoDto fetchEtcFishingInfo(String areaCode) {
        try {
            String url = API_URL + "?areaCode=" + areaCode;
            String json = restTemplate.getForObject(url, String.class);
            return parseResponse(json);
        } catch (Exception e) {
            log.error("기타 낚시정보 API 호출 실패", e);
            return null;
        }
    }

    private EtcFishingInfoDto parseResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            EtcFishingInfoDto dto = new EtcFishingInfoDto();
            dto.setPointName(root.path("sfPointName").asText());
            dto.setAddress(root.path("sfAddress").asText());
            dto.setArea(root.path("sfArea").asText());
            dto.setFishName(root.path("fishName").asText());
            dto.setFishType(root.path("sfFishType").asText());
            dto.setScoreIcon(root.path("scoreIcon").asText());
            dto.setKhoaStationId(root.path("khoaStId").asText());
            dto.setLatitude(root.path("sfLat").asDouble());
            dto.setLongitude(root.path("sfLon").asDouble());

            return dto;
        } catch (Exception e) {
            log.error("기타 낚시정보 JSON 파싱 오류", e);
            return null;
        }
    }
}
