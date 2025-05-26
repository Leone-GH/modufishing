// ... (이전 클래스들 생략)

// TideForecastService.java
package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TideForecastService {

    private static final String SERVICE_KEY = "r5iO3DlkKWJOveWMNt22HQ==";
    private static final String BASE_URL = "https://www.khoa.go.kr/api/oceangrid/tideObsPreTab/search.do";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final RestTemplate restTemplate;

    public List<TideForecast> getTideForecast(String obsCode, LocalDate targetDate) {
        try {
            String url = BASE_URL +
                    "?ServiceKey=" + SERVICE_KEY +
                    "&ResultType=json" +
                    "&ObsCode=" + obsCode +
                    "&Date=" + FORMATTER.format(targetDate);

            TideForecastResponse response = restTemplate.getForObject(url, TideForecastResponse.class);
            return response.getResult().getData();
        } catch (Exception e) {
            log.error("조석 예보 조회 실패: obsCode={}, date={}", obsCode, targetDate, e);
            return List.of();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TideForecastResponse {
        private Result result;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<TideForecast> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TideForecast {
        @JsonProperty("record_time")
        private String recordTime;

        @JsonProperty("tide_level")
        private String tideLevel;

        @JsonProperty("tide_code")
        private String tideCode; // H: 만조, L: 간조
    }
}
