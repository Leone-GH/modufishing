package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.dto.reservation.ReservationOrderRequestDto;
import com.fishtripplanner.entity.ReservationOrderEntity;
import com.fishtripplanner.service.ReservationOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class ReservationOrderController {

    private final ReservationOrderService reservationOrderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody ReservationOrderRequestDto dto) {
        ReservationOrderEntity saved = reservationOrderService.createOrder(dto);
        return ResponseEntity.ok(saved.getId());
    }
}