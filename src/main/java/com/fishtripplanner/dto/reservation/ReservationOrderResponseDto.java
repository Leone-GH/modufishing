package com.fishtripplanner.dto.reservation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationOrderResponseDto {

    private Long id;

    // 예약자 정보
    private String userNick;

    // 예약글 제목
    private String postTitle;

    // 예약 날짜 및 인원
    private LocalDate reservationDate;
    private String serviceTime; // 예: "07:00~16:30"
    private int count;          // 예약 인원 수
    private boolean paid;       // 결제 여부

    private LocalDateTime createdAt;


    // 정원 정보 (해당 날짜 기준)
    private int totalCapacity;      // 해당 날짜의 총 정원
    private int reservedCount;      // 결제 완료된 예약 인원 합계
    private int remainingCapacity;  // 남은 정원 (총 - 예약완료)
}
