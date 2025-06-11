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


@RequiredArgsConstructor
@Service
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

        int capacity = post.getAvailableDates().stream()
                .filter(d -> d.getAvailableDate().equals(date))
                .findFirst()
                .map(d -> d.getCapacity())
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜는 예약 불가"));

        // 예약된 총 인원 수를 합산
        Integer reservedCount = reservationOrderRepository.sumPaidCountByPostIdAndDate(post.getId(), date);

        if (reservedCount == null) reservedCount = 0;  // 예약이 없다면 0으로 처리

        int remaining = capacity - reservedCount;

        if (remaining < dto.getCount()) {
            throw new IllegalStateException("남은 자리가 부족합니다. 남은 자리: " + remaining);
        }

        ReservationOrderEntity order = ReservationOrderEntity.builder()
                .reservationPost(post)
                .user(user)
                .availableDate(date)
                .count(dto.getCount())
                .createdAt(LocalDate.now())
                .paid(dto.isPaid())
                .build();

        return reservationOrderRepository.save(order);
    }
}
