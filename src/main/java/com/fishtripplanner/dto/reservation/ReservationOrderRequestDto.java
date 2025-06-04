package com.fishtripplanner.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// 생성 요청 DTO
@Getter
@Setter
public class ReservationOrderRequestDto {
    private Long reservationPostId;
    private Long userId;
    private LocalDate availableDate;
    private int count;
    private boolean paid;
}