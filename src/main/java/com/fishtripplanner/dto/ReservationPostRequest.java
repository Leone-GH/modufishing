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
    private String region;
    private List<LocalDate> availableDates;
    private List<Long> fishTypeIds;
    private int price;
    private String imageUrl;
    private Long regionId; // ✅ FK용

    // ✅ DTO → Entity 변환 메서드 (클래스 내부에 위치해야 함)
    public ReservationPost toEntity(RegionEntity region) {
        return ReservationPost.builder()
                .title(this.title)
                .content(this.content)
                .price(this.price)
                .imageUrl(this.imageUrl)
                .type(ReservationType.valueOf(this.type))  // String → Enum
                .region(region)
                .build();
    }
}
