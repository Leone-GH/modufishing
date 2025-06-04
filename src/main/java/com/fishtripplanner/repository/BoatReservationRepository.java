package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.BoatReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoatReservationRepository extends JpaRepository<BoatReservation, Long> {
    List<BoatReservation> findByCompanyNameContainingIgnoreCaseOrRegionContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrFishTypesContainingIgnoreCaseOrBoatTypeContainingIgnoreCase(
            String keyword1, String keyword2, String keyword3, String keyword4, String keyword5);
}
