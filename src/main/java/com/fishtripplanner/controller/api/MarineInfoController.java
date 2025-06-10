package com.fishtripplanner.controller.api;

import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.service.MarineInfoService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marine-info")
@Slf4j
public class MarineInfoController {

    private final MarineInfoService marineInfoService;

    @PostMapping
    public MarineInfoResponseDto getMarineInfo(@RequestBody MarineInfoRequest request) {
        log.info("🌊 해양 정보 요청: 위도={}, 경도={}, 출발일={}, 도착시각={}",
                request.getDestinationLat(), request.getDestinationLng(), request.getDepartureDate(), request.getArrivalTime());

        return marineInfoService.fetchMarineInfo(
                request.getDestinationLat(),
                request.getDestinationLng(),
                request.getDepartureDate(),
                request.getArrivalTime()
        );
    }

    @Getter
    @Setter
    public static class MarineInfoRequest {
        private double destinationLat;
        private double destinationLng;
        private String departureDate;   // yyyy-MM-dd
        private String arrivalTime;     // HH:mm
    }
}

