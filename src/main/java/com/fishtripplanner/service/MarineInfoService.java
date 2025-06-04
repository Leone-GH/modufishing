package com.fishtripplanner.service;

import com.fishtripplanner.api.khoa.AreaCodes;
import com.fishtripplanner.api.khoa.LifeFishingIndexService;
import com.fishtripplanner.api.khoa.TripMarineInfoService;
import com.fishtripplanner.dto.FishingIndexDto;
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

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public MarineInfoResponseDto fetchMarineInfo(
            double lat, double lon, String departureDate, String arrivalTime
    ) {
        MarineInfoResponseDto response = new MarineInfoResponseDto();

        List<Callable<Void>> tasks = new ArrayList<>();

        // 생활낚시지수 - 갯바위(ROCK)
        tasks.add(() -> {
            List<FishingIndexDto> rockDtoList =
                    lifeFishingIndexService.fetchFishingIndex(lat, lon, departureDate, AreaCodes.AreaType.ROCK);
            response.setRock(rockDtoList);
            return null;
        });

        // 생활낚시지수 - 선상(BOAT)
        tasks.add(() -> {
            List<FishingIndexDto> boatDtoList =
                    lifeFishingIndexService.fetchFishingIndex(lat, lon, departureDate, AreaCodes.AreaType.BOAT);
            response.setBoat(boatDtoList);
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

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            log.error("❌ 병렬 호출 중 오류 발생", e);
        }

        return response;
    }
}
