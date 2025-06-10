package com.fishtripplanner.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EtcFishingIndex {
    private String pointName;      // 포인트 이름
    private String fishName;       // 어종 이름
    private String fishingIndex;   // 낚시 지수 (좋음/보통/나쁨 등)
    private String waveHeight;     // 파고
    private String waterTemp;      // 수온
    private String currentSpeed;   // 조류 속도
    private String airTemp;        // 기온
}
