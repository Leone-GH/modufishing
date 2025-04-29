package com.fishtripplanner.repository;


import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationPostRepository extends JpaRepository<ReservationPost, Long> {

    List<ReservationPost> findByOwnerId(Long ownerId);

    List<ReservationPost> findByType(ReservationType type); // List용

    Page<ReservationPost> findByType(ReservationType type, Pageable pageable); // 페이징용

    @Query("SELECT r FROM ReservationPost r WHERE r.type = :type AND r.region.id IN :regionIds")
    Page<ReservationPost> findByTypeAndRegionIds(@Param("type") ReservationType type,
                                                 @Param("regionIds") List<Long> regionIds,
                                                 Pageable pageable);

    @Query("SELECT DISTINCT r.region FROM ReservationPost r")
    List<String> findAllRegions(); // 선택 사항
}
