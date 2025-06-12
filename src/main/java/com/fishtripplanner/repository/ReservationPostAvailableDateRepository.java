package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationPostAvailableDateRepository extends JpaRepository<ReservationPostAvailableDate, Long> {
    void deleteAllByReservationPost(ReservationPost post);
}