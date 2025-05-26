// FishingIndexService.java
package com.fishtripplanner.api.khoa;
import com.fishtripplanner.api.khoa.FishingIndexRegionType;

import com.fishtripplanner.api.khoa.FishingIndexResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FishingIndexService {

    private final RestTemplate restTemplate;

    @Value("${api.fishing-index-key}")
    private String serviceKey;

    private static final String BASE_URL = "https://www.khoa.go.kr/api/oceangrid/fishingGear/getFishingIndex.do";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FishingIndexService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FishingIndex> getFishingIndex(String region, LocalDate date) {
        return com.fishtripplanner.api.khoa.FishingIndexRegionType.mapToApiRegion(region).map(apiRegion -> {
            try {
                String url = BASE_URL +
                        "?ServiceKey=" + serviceKey +
                        "&ResultType=json" +
                        "&Area=" + apiRegion +
                        "&Date=" + FORMATTER.format(date);

                ResponseEntity<FishingIndexResponse<FishingIndex>> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<FishingIndexResponse<FishingIndex>>() {}
                        );

                @SuppressWarnings("unchecked")
                List<FishingIndex> resultList = Optional.ofNullable(response.getBody())
                        .map(FishingIndexResponse::getResult)
                        .map(FishingIndexResult::getData)
                        .orElse(Collections.emptyList());

                return resultList;

            } catch (Exception e) {
                log.error("생활해양예보지수(갯바위낚시) 조회 실패: area={}, date={}", region, date, e);
                return List.of();
            }
        }).orElse(List.of());
    }

    public int mapFishingIndexToScore(String index) {
        return switch (index) {
            case "매우좋음" -> 5;
            case "좋음" -> 4;
            case "보통" -> 3;
            case "나쁨" -> 2;
            case "매우나쁨" -> 1;
            default -> 0;
        };
    }

    public Optional<FishingIndex> recommendBestTime(List<FishingIndex> list, String fishType) {
        return list.stream()
                .filter(f -> f.getFishType().equalsIgnoreCase(fishType))
                .max(Comparator.comparing(FishingIndex::getFishingIndex));
    }

    public Optional<FishingIndex> recommendBestTimeAfter(List<FishingIndex> list, String fishType, LocalTime afterTime) {
        return list.stream()
                .filter(f -> f.getFishType().equalsIgnoreCase(fishType))
                .filter(f -> {
                    try {
                        return afterTime == null || LocalTime.parse(f.getTime()).isAfter(afterTime);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .max(Comparator.comparing(FishingIndex::getFishingIndex));
    }
}
