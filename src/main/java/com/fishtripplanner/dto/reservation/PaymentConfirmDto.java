package com.fishtripplanner.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentConfirmDto {
    private Long reservationPostId;
    private Long userId;
    private LocalDate reservationDate; // ✅ 이름 변경
    private int count;
    private boolean paid;

    // ✅ 생성자 수정
    public PaymentConfirmDto(Long reservationPostId, Long userId, String date, int count, boolean paid) {
        this.reservationPostId = reservationPostId;
        this.userId = userId;
        this.reservationDate = LocalDate.parse(date);
        this.count = count;
        this.paid = paid;
    }

    public ReservationOrderRequestDto toReservationOrderRequestDto() {
        return ReservationOrderRequestDto.builder()
                .reservationPostId(reservationPostId)
                .userId(userId)
                .reservationDate(reservationDate) // ✅ 여기 이름 변경
                .count(count)
                .paid(paid)
                .build();
    }
}
