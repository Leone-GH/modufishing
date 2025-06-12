package com.fishtripplanner.repository;

import com.fishtripplanner.entity.ReservationOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ReservationOrderRepository extends JpaRepository<ReservationOrderEntity, Long> {

    // ✅ 특정 예약글 + 날짜의 결제 완료 인원 합계
    @Query("SELECT SUM(o.count) FROM ReservationOrderEntity o " +
            "WHERE o.reservationPost.id = :postId " +
            "AND o.reservationDate = :availableDate " +
            "AND o.paid = true")
    Integer sumPaidCountByPostIdAndDate(@Param("postId") Long postId,
                                        @Param("availableDate") LocalDate availableDate);

    // ✅ 특정 예약글의 날짜별 예약자 수 합계 (GROUP BY)
    @Query("SELECT o.reservationDate, SUM(o.count) " +
            "FROM ReservationOrderEntity o " +
            "WHERE o.reservationPost.id = :postId AND o.paid = true " +
            "GROUP BY o.reservationDate")
    List<Object[]> findPaidCountsByPostGroupedByDateRaw(@Param("postId") Long postId);

    // ✅ Object[] 결과를 Map<LocalDate, Integer>로 변환
    default Map<LocalDate, Integer> findPaidCountsByPostGroupedByDate(Long postId) {
        List<Object[]> raw = findPaidCountsByPostGroupedByDateRaw(postId);
        Map<LocalDate, Integer> result = new HashMap<>();
        for (Object[] row : raw) {
            result.put((LocalDate) row[0], ((Long) row[1]).intValue());
        }
        return result;
    }

    // ✅ ❗ 지금 문제 해결용 메서드 추가
    List<ReservationOrderEntity> findAllByReservationPost_Id(Long postId);
}
