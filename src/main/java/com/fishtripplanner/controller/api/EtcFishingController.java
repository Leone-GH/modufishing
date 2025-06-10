package com.fishtripplanner.controller.api;

import com.fishtripplanner.api.khoa.EtcFishingService;
import com.fishtripplanner.dto.EtcFishingInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/info/etc")
public class EtcFishingController {

    private final EtcFishingService etcFishingService;

    @GetMapping
    public EtcFishingInfoDto getEtcInfo(@RequestParam String areaCode) {
        return etcFishingService.fetchEtcFishingInfo(areaCode);
    }
}