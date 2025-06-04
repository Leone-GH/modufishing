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

    public ReservationOrderEntity  createOrder(ReservationOrderRequestDto dto) {
        ReservationPost post = reservationPostRepository.findById(dto.getReservationPostId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Post ID"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid User ID"));

        ReservationOrderEntity order = ReservationOrderEntity.builder()
                .reservationPost(post)
                .user(user)
                .availableDate(dto.getAvailableDate())
                .count(dto.getCount())
                .reservedAt(LocalDate.now())
                .paid(dto.isPaid())
                .build();

        return reservationOrderRepository.save(order);
    }
}