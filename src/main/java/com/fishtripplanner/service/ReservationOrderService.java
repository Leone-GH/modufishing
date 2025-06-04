package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.dto.reservation.ReservationOrderRequestDto;
import com.fishtripplanner.entity.ReservationOrderEntity;
import com.fishtripplanner.repository.ReservationOrderRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import com.fishtripplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservationOrderService {

    private final ReservationOrderRepository reservationOrderRepository;
    private final ReservationPostRepository reservationPostRepository;
    private final UserRepository userRepository;

    public ReservationOrderEntity createOrder(ReservationOrderRequestDto dto) {
        ReservationPost post = reservationPostRepository.findById(dto.getReservationPostId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Post ID"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid User ID"));

        LocalDate date = dto.getAvailableDate();

        // 🔍 정원 확인용: 해당 날짜에 대한 capacity 가져오기
        int capacity = post.getAvailableDates().stream()
                .filter(d -> d.getAvailableDate().equals(date))
                .findFirst()
                .map(d -> d.getCapacity())
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜는 예약 불가"));

        // ✅ 남은 자리 계산
        int reservedCount = reservationOrderRepository.countByReservationPostAndAvailableDate(post, date);
        int remaining = capacity - reservedCount;

        if (remaining < dto.getCount()) {
            throw new IllegalStateException("남은 자리가 부족합니다. 남은 자리: " + remaining);
        }

        // ✅ 정상 예약 진행
        ReservationOrderEntity order = ReservationOrderEntity.builder()
                .reservationPost(post)
                .user(user)
                .availableDate(date)
                .count(dto.getCount())
                .reservedAt(LocalDate.now())
                .paid(dto.isPaid())
                .build();

        return reservationOrderRepository.save(order);
    }

}