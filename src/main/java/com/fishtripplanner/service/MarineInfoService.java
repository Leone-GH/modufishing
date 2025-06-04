package com.fishtripplanner.service;

import com.fishtripplanner.api.khoa.AreaCodes;
import com.fishtripplanner.api.khoa.LifeFishingIndexService;
import com.fishtripplanner.api.khoa.TripMarineInfoService;
import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.dto.RecommendedFishingTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarineInfoService {

    private final LifeFishingIndexService lifeFishingIndexService;
    private final TripMarineInfoService tripMarineInfoService;
    // 필요하다면 기타 서비스(EtcFishingService 등)도 추가

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public MarineInfoResponseDto fetchMarineInfo(
            double lat, double lon, String departureDate, String arrivalTime
    ) {
        MarineInfoResponseDto response = new MarineInfoResponseDto();

        List<Callable<Void>> tasks = new ArrayList<>();

        // 생활낚시지수 - 갯바위(ROCK)
        tasks.add(() -> {
            List<LifeFishingIndexService.FishingIndex> rockList =
                    lifeFishingIndexService.fetchFishingIndex(lat, lon, departureDate, AreaCodes.AreaType.ROCK);
            response.setRock(rockList);
            return null;
        });

        // 생활낚시지수 - 선상(BOAT)
        tasks.add(() -> {
            List<LifeFishingIndexService.FishingIndex> boatList =
                    lifeFishingIndexService.fetchFishingIndex(lat, lon, departureDate, AreaCodes.AreaType.BOAT);
            response.setBoat(boatList);
            return null;
        });

        // 해양 예보(TRIP)
        tasks.add(() -> {
            MarineInfoResponseDto tripDto =
                    tripMarineInfoService.getMarineInfo(lat, lon, departureDate, arrivalTime);
            List<RecommendedFishingTime> timeList = tripDto.getRecommendedTimes();
            response.setRecommendedTimes(timeList);
            return null;
        });

        // 필요하다면 기타 생활낚시지수(ETC)도 여기에 추가

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            log.error("❌ 병렬 호출 중 오류 발생", e);
        }

        return response;
    }
}
