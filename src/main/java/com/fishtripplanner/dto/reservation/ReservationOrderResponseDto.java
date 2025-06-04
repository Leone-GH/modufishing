package com.fishtripplanner.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// 응답 DTO

@Getter
public class ReservationOrderResponseDto {
    private Long id;
    private String userNick;
    private String postTitle;
    private LocalDate availableDate;
    private int count;
    private boolean paid;
}
