package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.entity.ReservationOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ReservationOrderRepository extends JpaRepository<ReservationOrderEntity, Long> {

    // ✅ 추천 방식: 메서드 이름 기반 파생 쿼리
    int countByReservationPostAndAvailableDate(ReservationPost reservationPost, LocalDate availableDate);

}
