package com.fishtripplanner.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FishingIndexDto {
    private String dateStr;
    private String timeStr;
    private String fishName;
    private String fishingIndex;
    private double fishingScore;
    private String waveHeight;
    private String currentSpeed;
    private String waterTemp;
    private String airTemp;
    private String tide;
}
