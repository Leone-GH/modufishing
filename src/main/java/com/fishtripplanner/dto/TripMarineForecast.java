package com.fishtripplanner.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TripMarineForecast {
    private final String date;
    private final String time;
    private final String fishType;
    private final String waterTemp;
    private final String waveHeight;
    private final String airTemp;
    private final String windSpeed;
    private final String weather;
    private final String fishingIndex;
    private final String area;
    private final String location;
    private final double stLat;
    private final double stLon;
    private final String fishingScore;

    @Builder
    public TripMarineForecast(
            String date,
            String time,
            String fishType,
            String waterTemp,
            String waveHeight,
            String airTemp,
            String windSpeed,
            String weather,
            String fishingIndex,
            String area,
            String location,
            double stLat,
            double stLon,
            String fishingScore
    ) {
        this.date = date;
        this.time = time;
        this.fishType = fishType;
        this.waterTemp = waterTemp;
        this.waveHeight = waveHeight;
        this.airTemp = airTemp;
        this.windSpeed = windSpeed;
        this.weather = weather;
        this.fishingIndex = fishingIndex;
        this.area = area;
        this.location = location;
        this.stLat = stLat;
        this.stLon = stLon;
        this.fishingScore = fishingScore;
    }
}

