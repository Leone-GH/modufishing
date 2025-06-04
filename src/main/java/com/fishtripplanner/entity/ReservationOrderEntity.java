package com.fishtripplanner.entity;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
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

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(nullable = false)
    private int count;

    @Column(name = "reserved_at", nullable = false)
    private LocalDate reservedAt;

    @Column(nullable = false)
    private boolean paid;
}