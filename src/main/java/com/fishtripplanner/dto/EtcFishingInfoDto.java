package com.fishtripplanner.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EtcFishingInfoDto {
    private String pointName;       // 포인트 명 (예: "울진 후정")
    private String address;         // 주소
    private String area;            // 지역명 (예: "동해")
    private String fishName;        // 출현 어종들
    private String fishType;        // 어종 유형 (예: "LF")
    private String scoreIcon;       // 낚시 지수 아이콘명 (예: "좋음")
    private String khoaStationId;   // KHOA 관측소 ID
    private double latitude;        // 위도
    private double longitude;       // 경도
}