package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationPostRepository extends JpaRepository<ReservationPost, Long> {

    List<ReservationPost> findByOwnerId(Long ownerId);
    List<ReservationPost> findByType(ReservationType type);
    Page<ReservationPost> findByType(ReservationType type, Pageable pageable);

    @Query("""
        SELECT r
        FROM ReservationPost r
        WHERE r.type = :type
          AND r.region.id IN :regionIds
    """)
    Page<ReservationPost> findByTypeAndRegionIds(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.fishTypes f
        WHERE r.type = :type
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByFishTypes(
            @Param("type") ReservationType type,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 수정: 날짜 IN 조건 추가
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        JOIN r.fishTypes f
        WHERE r.type = :type
          AND d.availableDate IN :dates
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByDateAndFishTypes(
            @Param("type") ReservationType type,
            @Param("dates") List<LocalDate> dates,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 수정: 날짜 IN 조건 추가
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND d.availableDate IN :dates
    """)
    Page<ReservationPost> findByTypeAndRegionIdsAndDate(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("dates") List<LocalDate> dates,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.fishTypes f
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByRegionIdsAndFishTypes(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 수정: 날짜 IN 조건 추가
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        JOIN r.fishTypes f
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND d.availableDate IN :dates
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByFiltersStrict(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("dates") List<LocalDate> dates,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 수정: 날짜 IN 조건 추가
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        WHERE r.type = :type
          AND d.availableDate IN :dates
    """)
    Page<ReservationPost> findByTypeAndDate(
            @Param("type") ReservationType type,
            @Param("dates") List<LocalDate> dates,
            Pageable pageable
    );

    // ✅ 수정: 느슨한 조건 필터 - 날짜 IN 사용
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        LEFT JOIN r.availableDates d
        LEFT JOIN r.fishTypes f
        WHERE r.type = :type
          AND (:regionIds IS NULL OR r.region.id IN :regionIds)
          AND (:dates IS NULL OR d.availableDate IN :dates)
          AND (:fishTypes IS NULL OR f.name IN :fishTypes)
          AND (
              :keyword IS NULL
               OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR EXISTS (
                   SELECT 1
                   FROM r.fishTypes f2
                   WHERE LOWER(f2.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               )
               OR LOWER(r.region.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<ReservationPost> findByFilters(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("dates") List<LocalDate> dates,
            @Param("fishTypes") List<String> fishTypes,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT DISTINCT f.name FROM FishTypeEntity f")
    List<String> findAllFishTypeNames();

    @Query("SELECT DISTINCT r.region.name FROM ReservationPost r")
    List<String> findAllRegionNames();

    @Query("""
        SELECT r FROM ReservationPost r
        LEFT JOIN FETCH r.availableDates
        WHERE r.id = :id
    """)
    Optional<ReservationPost> findByIdWithAvailableDatesOnly(@Param("id") Long id);
}
