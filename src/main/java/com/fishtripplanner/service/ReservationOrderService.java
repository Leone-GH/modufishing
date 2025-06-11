package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import com.fishtripplanner.dto.reservation.ReservationOrderRequestDto;
import com.fishtripplanner.entity.ReservationOrderEntity;
import com.fishtripplanner.repository.ReservationOrderRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import com.fishtripplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ReservationOrderService {

    private final ReservationOrderRepository reservationOrderRepository;
    private final ReservationPostRepository reservationPostRepository;
    private final UserRepository userRepository;

    /**
     * ✅ 예약 주문 생성
     * - 좌석 수 확인
     * - 날짜 및 시간 확인
     * - 유저/예약글 유효성 체크
     */
    public ReservationOrderEntity createOrder(ReservationOrderRequestDto dto) {
        // 🔹 예약글 조회
        ReservationPost post = reservationPostRepository.findById(dto.getReservationPostId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Post ID"));

        // 🔹 사용자 조회
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid User ID"));

        // 🔹 예약 날짜 확인
        LocalDate reservationDate = dto.getReservationDate();

        // 🔹 해당 날짜의 예약 정보 추출
        ReservationPostAvailableDate matchedDate = post.getAvailableDates().stream()
                .filter(d -> d.getAvailableDate().equals(reservationDate))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜는 예약 불가"));

        int capacity = matchedDate.getCapacity();
        String serviceTime = matchedDate.getTime();

        // 🔹 현재까지 예약된 인원 수
        Integer reservedCount = reservationOrderRepository.sumPaidCountByPostIdAndDate(post.getId(), reservationDate);
        if (reservedCount == null) reservedCount = 0;

        int remaining = capacity - reservedCount;
        if (remaining < dto.getCount()) {
            throw new IllegalStateException("남은 자리가 부족합니다. 남은 자리: " + remaining);
        }

        // 🔹 예약 엔티티 생성 및 저장
        ReservationOrderEntity order = ReservationOrderEntity.builder()
                .reservationPost(post)
                .user(user)
                .reservationDate(reservationDate)
                .serviceTime(serviceTime)
                .count(dto.getCount())
                .createdAt(LocalDateTime.now().withNano(0))
                .paid(dto.isPaid())
                .build();

        return reservationOrderRepository.save(order);
    }
}