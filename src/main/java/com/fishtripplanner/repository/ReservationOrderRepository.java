package com.fishtripplanner.repository;

import com.fishtripplanner.entity.ReservationOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationOrderRepository extends JpaRepository<ReservationOrderEntity, Long> {
    // 추후 custom query 추가 예정
}