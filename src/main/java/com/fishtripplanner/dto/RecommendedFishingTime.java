package com.fishtripplanner.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedFishingTime {
    private String date;
    private String time;
    private String fishName;
    private String waterTemp;
    private String waveHeight;
    private String airTemp;
    private String currentSpeed;
    private String windSpeed;
    private String weather;
    private String fishingIndex;
    private String area;
    private String location;
    private double stLat;
    private double stLon;
    private String fishingScore;
    private String tide;
}
