package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 외부 낚시 지수 API의 단일 지점 데이터 구조.
 * JSON 응답 필드와 1:1 매핑되며, 낚시 지수 예측에 필요한 주요 해양 기상 요소 포함.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 예기치 않은 필드 무시
public class FishingIndex {

    /** 지역명 (예: 부산, 인천 등) */
    @JsonProperty("area")
    private String area;

    /** 날짜 (예: 2025-05-26) */
    @JsonProperty("date")
    private String date;

    /** 시간 (예: 14:00) */
    @JsonProperty("time")
    private String time;

    /** 대상 어종 (예: 광어, 우럭 등) */
    @JsonProperty("fish_type")
    private String fishType;

    /** 파고 (단위: m, 예: 0.5) */
    @JsonProperty("wave_height")
    private String waveHeight;

    /** 수온 (단위: ℃, 예: 16.3) */
    @JsonProperty("water_temp")
    private String waterTemp;

    /** 기온 (단위: ℃, 예: 18.2) */
    @JsonProperty("air_temp")
    private String airTemp;

    /** 조류 속도 (단위: m/s, 예: 0.3) */
    @JsonProperty("current_speed")
    private String currentSpeed;

    /** 조석 정보 (예: 만조, 간조 등) */
    @JsonProperty("tide")
    private String tide;

    /** 낚시 지수 (예: 3, 8 등급 - 정수형 문자열) */
    @JsonProperty("fishing_index")
    private String fishingIndex;
}
