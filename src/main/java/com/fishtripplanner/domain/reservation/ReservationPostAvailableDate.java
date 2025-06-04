package com.fishtripplanner.domain.reservation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * ✅ 예약 가능한 날짜를 나타내는 엔티티
 * - ReservationPost 와 다대일 관계
 * - 예약글마다 여러 개의 예약 가능 날짜를 가질 수 있음
 */
@Entity
@Table(name = "reservation_post_available_dates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPostAvailableDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ✅ 연관된 예약글
     */
    @ManyToOne
    @JoinColumn(name = "reservation_post_id")
    private ReservationPost reservationPost;

    /**
     * ✅ 예약 가능한 날짜
     */
    @Column(name = "available_date")
    private LocalDate availableDate;

    /**
     * ✅ 예약 정원
     */
    @Column(name = "capacity")
    private Integer capacity;

    /**
     * ✅ 예약 가능 시간 (예: 06:00~14:00)
     */
    @Column(name = "time")
    private String time;
}
