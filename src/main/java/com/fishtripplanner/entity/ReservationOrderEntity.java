package com.fishtripplanner.entity;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_order")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 예약글과 다대일
    @JoinColumn(name = "reservation_post_id", nullable = false)
    private ReservationPost reservationPost;

    @ManyToOne(fetch = FetchType.LAZY) // 사용자와 다대일
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;  // ✅ 예약 날짜

    @Column(nullable = false)
    private int count;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;  // ✅ 예약 생성 시간

    @Column(nullable = false)
    private boolean paid;

    @Column(name = "service_time", nullable = false)
    private String serviceTime;  // ✅ 예약 이행 시간 (ex. 07:30~16:30)
}
