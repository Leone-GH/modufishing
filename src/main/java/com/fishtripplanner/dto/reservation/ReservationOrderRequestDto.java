package com.fishtripplanner.dto.reservation;

import lombok.*;

import java.time.LocalDate;

// 생성 요청 DTO
@Getter
@Setter
@Builder // ✅ 추가
@NoArgsConstructor
@AllArgsConstructor
public class ReservationOrderRequestDto {
    private Long reservationPostId;
    private Long userId;
    private LocalDate reservationDate;
    private int count;
    private boolean paid;
}