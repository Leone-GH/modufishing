package com.fishtripplanner.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedFishingTime {
    private String date;
    private String time;
    private String fishType;
    private String waterTemp;
    private String waveHeight;
    private String airTemp;
    private String currentSpeed;  // TRIP 타입은 null
    private String tide;          // TRIP 타입은 null
    private String fishingIndex;
    private int fishingScore;
}
