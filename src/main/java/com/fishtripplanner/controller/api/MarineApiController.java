package com.fishtripplanner.controller.api;

import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.service.MarineInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marine")
public class MarineApiController {

    private final MarineInfoService marineInfoService;

    @GetMapping
    public MarineInfoResponseDto getMarineInfo(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String departureDate,
            @RequestParam String arrivalTime
    ) {
        return marineInfoService.fetchMarineInfo(lat, lon, departureDate, arrivalTime);
    }
}
