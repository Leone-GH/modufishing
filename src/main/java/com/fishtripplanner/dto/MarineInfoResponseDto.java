package com.fishtripplanner.dto;

import com.fishtripplanner.api.khoa.LifeFishingIndexService.FishingIndex;
import com.fishtripplanner.dto.RecommendedFishingTime;
import com.fishtripplanner.dto.TripMarineForecast;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MarineInfoResponseDto {
    private List<FishingIndex> rock = new ArrayList<>(); // 갯바위 생활낚시지수
    private List<FishingIndex> boat = new ArrayList<>(); // 선상 생활낚시지수
    private List<FishingIndex> etc = new ArrayList<>();  // 기타(보조) 낚시지수
    private List<TripMarineForecast> trip = new ArrayList<>(); // 여행지용 해양 예보

    private List<RecommendedFishingTime> recommendedTimes = new ArrayList<>();

    public void addRecommendedTime(RecommendedFishingTime time) {
        this.recommendedTimes.add(time);
    }
}
