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

    // ✅ 추가된 생성자
    public PaymentConfirmDto(Long reservationPostId, Long userId, String date, int count, boolean paid) {
        this.reservationPostId = reservationPostId;
        this.userId = userId;
        this.availableDate = LocalDate.parse(date);
        this.count = count;
        this.paid = paid;
    }

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
