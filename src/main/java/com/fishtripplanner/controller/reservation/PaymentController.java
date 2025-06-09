package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.dto.reservation.PaymentConfirmDto;
import com.fishtripplanner.entity.ReservationOrderEntity;
import com.fishtripplanner.repository.ReservationPostRepository;
import com.fishtripplanner.service.ReservationOrderService;
import com.fishtripplanner.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final ReservationOrderService reservationOrderService;
    private final ReservationPostRepository reservationPostRepository;

    // ✅ 예약 결제 페이지 진입
    @GetMapping("/reservation/payment")
    public String showPaymentPage(@RequestParam Long postId,
                                  @RequestParam String date,
                                  @RequestParam int count,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid postId"));

        model.addAttribute("post", post);
        model.addAttribute("date", date);
        model.addAttribute("count", count);
        model.addAttribute("userId", userDetails.getUser().getId());

        return "reservation_page/reservation_payment";  // 결제 확인 페이지
    }

    // ✅ 결제 완료 처리
    @PostMapping("/payment/confirm")
    public String confirmPayment(@ModelAttribute PaymentConfirmDto dto) {
        ReservationOrderEntity order = reservationOrderService.createOrder(dto.toReservationOrderRequestDto());
        return "redirect:/reservation/complete";
    }

    // ✅ 결제 완료 안내 페이지
    @GetMapping("/reservation/complete")
    public String reservationCompletePage() {
        return "reservation_page/reservation_complete";
    }
}

