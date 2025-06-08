package com.fishtripplanner.repository;

import com.fishtripplanner.entity.ReservationOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ReservationOrderRepository extends JpaRepository<ReservationOrderEntity, Long> {

    // 예약된 인원 총합을 구하는 쿼리
    @Query("SELECT SUM(o.count) FROM ReservationOrderEntity o WHERE o.reservationPost.id = :postId AND o.availableDate = :availableDate")
    Integer sumCountByPostIdAndDate(@Param("postId") Long postId, @Param("availableDate") LocalDate availableDate);
}
