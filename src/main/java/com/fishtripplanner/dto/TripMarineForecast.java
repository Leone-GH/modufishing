package com.fishtripplanner.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripMarineForecast {
    private String fcstArea;
    private String predDate;
    private String predType;
    private String strWeather;
    private String avgWindSpeed;
    private String avgWaveHeight;
    private String avgWaterTemp;
    private String avgAirTemp;
    private String totalScoreStr;

    // 기존에 생성자가 아래처럼 되어 있으면
    // TripMarineForecast(String, String, ...)

    // 반드시 public을 명시해야 함
    public TripMarineForecast(
            String fcstArea,
            String predDate,
            String predType,
            String strWeather,
            String avgWindSpeed,
            String avgWaveHeight,
            String avgWaterTemp,
            String avgAirTemp,
            String totalScoreStr
    ) {
        this.fcstArea = fcstArea;
        this.predDate = predDate;
        this.predType = predType;
        this.strWeather = strWeather;
        this.avgWindSpeed = avgWindSpeed;
        this.avgWaveHeight = avgWaveHeight;
        this.avgWaterTemp = avgWaterTemp;
        this.avgAirTemp = avgAirTemp;
        this.totalScoreStr = totalScoreStr;
    }
}
