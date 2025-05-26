// TripMarineInfoService.java
package com.fishtripplanner.api.khoa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripMarineInfoService {

    private final KhoaStationService khoaStationService;
    private final FishingIndexService fishingIndexService;
    private final TideForecastService tideForecastService;

    public MarineInfoResult getMarineInfo(double lat, double lon, String areaName, LocalDate targetDate, String fishType) {
        MarineInfoResult result = new MarineInfoResult();

        // 1. 낚시 지수 정보 조회
        List<FishingIndex> fishingIndices = fishingIndexService.getFishingIndex(areaName, targetDate);
        result.setFishingIndexList(fishingIndices);

        // 2. 추천 시간 계산
        Optional<FishingIndex> recommended = Optional.empty();
        if (fishType != null) {
            recommended = fishingIndexService.recommendBestTimeAfter(fishingIndices, fishType, null);
        }

        // 3. 해양 관측소 데이터
        List<String> requiredTypes = List.of("수온", "풍속", "풍향", "기온");
        Map<String, Optional<KhoaStationService.Station>> stationMap =
                khoaStationService.findNearestStationsForDataTypes(lat, lon, requiredTypes);

        stationMap.forEach((type, stationOpt) -> stationOpt.ifPresent(s -> result.addStation(type, s)));

        // 4. 조석 예보 정보 (수온 관측소 기준)
        Optional<KhoaStationService.Station> tideStationOpt = stationMap.getOrDefault("수온", Optional.empty());
        tideStationOpt.ifPresent(station -> {
            result.setTideForecastList(
                    tideForecastService.getTideForecast(station.getObsPostId(), targetDate)
            );
        });

        // 5. 추천 결과 포함
        result.setRecommended(recommended);

        return result;
    }

    @lombok.Data
    public static class MarineInfoResult {
        private List<FishingIndex> fishingIndexList;
        private Map<String, KhoaStationService.Station> stationByType = new java.util.HashMap<>();
        private List<TideForecastService.TideForecast> tideForecastList = new java.util.ArrayList<>();
        private Optional<FishingIndex> recommended = Optional.empty();

        public void addStation(String type, KhoaStationService.Station station) {
            stationByType.put(type, station);
        }
    }
}
