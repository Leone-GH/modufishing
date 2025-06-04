package com.fishtripplanner.dto;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.entity.RegionEntity;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ReservationPostRequest {
    private Long ownerId;
    private String type;
    private String title;
    private String content;
    private List<LocalDate> availableDates;
    private List<Long> fishTypeIds;
    private int price;
    private String imageUrl;
    private List<Long> regionIds;  // ✅ 여러 지역을 지원하도록 변경

    // ✅ 서비스에서 루프 돌려서 여러 지역을 사용하도록 변경
    public ReservationPost toEntity(List<RegionEntity> regions) {
        // 여러 지역을 하나의 예약글에 연결하기 위해서
        return ReservationPost.builder()
                .title(this.title)
                .content(this.content)
                .price(this.price)
                .imageUrl(this.imageUrl)
                .type(ReservationType.valueOf(this.type))
                .regions(regions)  // 여러 지역을 예약글에 연결
                .build();
    }
}
