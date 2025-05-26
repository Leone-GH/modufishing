// ... (이전 클래스들 생략)

// KhoaStationService.java
package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class KhoaStationService {

    private static final String SERVICE_KEY = "r5iO3DlkKWJOveWMNt22HQ==";
    private static final String STATION_LIST_URL = "http://www.khoa.go.kr/api/oceangrid/ObsServiceObj/search.do?ServiceKey=" + SERVICE_KEY + "&ResultType=json";

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Station> getAllStations() {
        try {
            String rawJson = restTemplate.getForObject(STATION_LIST_URL, String.class);
            ObjectMapper mapper = new ObjectMapper();
            StationApiResponse response = mapper.readValue(rawJson, StationApiResponse.class);
            return response.getResult().getData();
        } catch (Exception e) {
            log.error("관측소 목록 불러오기 실패", e);
            return List.of();
        }
    }

    public Map<String, Optional<Station>> findNearestStationsForDataTypes(double lat, double lon, List<String> requiredTypes) {
        List<Station> stations = getAllStations();
        Map<String, Optional<Station>> result = new HashMap<>();

        for (String type : requiredTypes) {
            result.put(type, stations.stream()
                    .filter(station -> station.getObsObject() != null && station.getObsObject().contains(type))
                    .min(Comparator.comparingDouble(station -> haversine(lat, lon, station.getObsLat(), station.getObsLon())))
            );
        }

        return result;
    }

    public Optional<Station> findNearestStationWithWaterTemp(double lat, double lon) {
        return getAllStations().stream()
                .filter(station -> station.getObsObject() != null && station.getObsObject().contains("수온"))
                .min(Comparator.comparingDouble(station -> haversine(lat, lon, station.getObsLat(), station.getObsLon())));
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StationApiResponse {
        private Result result;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<Station> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Station {
        @JsonProperty("obs_post_id")
        private String obsPostId;

        @JsonProperty("data_type")
        private String dataType;

        @JsonProperty("obs_post_name")
        private String obsPostName;

        @JsonProperty("obs_lat")
        private double obsLat;

        @JsonProperty("obs_lon")
        private double obsLon;

        @JsonProperty("obs_object")
        private String obsObject;
    }
}
