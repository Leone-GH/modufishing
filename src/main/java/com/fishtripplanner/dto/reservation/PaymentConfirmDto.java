package com.fishtripplanner.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentConfirmDto {
    private Long reservationPostId;
    private Long userId;
    private LocalDate availableDate;
    private int count;
    private boolean paid;

    public ReservationOrderRequestDto toReservationOrderRequestDto() {
        return ReservationOrderRequestDto.builder()
                .reservationPostId(reservationPostId)
                .userId(userId)
                .availableDate(availableDate)
                .count(count)
                .paid(paid)
                .build();
    }
}