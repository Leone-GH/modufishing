package com.fishtripplanner.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ReservationPostRequest {
    private Long ownerId;
    private String type;
    private String title;
    private String content;
    @Getter
    private String region;
    private List<LocalDate> availableDates;
    private int price;
    private String imageUrl;
    private Long regionId; // ✅ 추가

}