package com.fishtripplanner.controller.api;

import com.fishtripplanner.api.khoa.FishingIndex;
import com.fishtripplanner.api.khoa.FishingIndexService;
import com.fishtripplanner.api.khoa.TripMarineInfoService;
import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.mapper.MarineInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/marine-info")
@RequiredArgsConstructor
public class MarineInfoController {

    private final TripMarineInfoService tripMarineInfoService;
    private final FishingIndexService fishingIndexService;

    @GetMapping
    public MarineInfoResponseDto getMarineInfo(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String area,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String fishType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime arrivalTime
    ) {
        var result = tripMarineInfoService.getMarineInfo(lat, lon, area, date);

        Optional<FishingIndex> recommended = Optional.empty();
        if (fishType != null) {
            if (arrivalTime != null) {
                recommended = fishingIndexService.recommendBestTimeAfter(result.getFishingIndexList(), fishType, arrivalTime);
            } else {
                recommended = fishingIndexService.recommendBestTime(result.getFishingIndexList(), fishType);
            }
        }

        return MarineInfoMapper.toResponseDto(result, recommended);
    }
}
