package com.fishtripplanner.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MarineInfoResponseDto {
    private List<FishingIndexDto> rock = new ArrayList<>(); // 갯바위 생활낚시지수
    private List<FishingIndexDto> boat = new ArrayList<>(); // 선상 생활낚시지수
    private List<FishingIndexDto> etc = new ArrayList<>();  // 기타(보조) 낚시지수
    private List<TripMarineForecast> trip = new ArrayList<>(); // 여행지용 해양 예보

    private List<RecommendedFishingTime> recommendedTimes = new ArrayList<>();

    public void addRecommendedTime(RecommendedFishingTime time) {
        this.recommendedTimes.add(time);
    }
}
