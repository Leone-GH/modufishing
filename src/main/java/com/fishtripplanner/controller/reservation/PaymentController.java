package com.fishtripplanner.controller.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.dto.reservation.PaymentConfirmDto;
import com.fishtripplanner.entity.ReservationOrderEntity;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.ReservationOrderService;
import com.fishtripplanner.service.ReservationPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final ReservationOrderService reservationOrderService;
    private final ReservationPostService reservationPostService;

    @GetMapping("/reservation/payment")
    public String showPaymentPage(@RequestParam Long postId,
                                  @RequestParam String date,
                                  @RequestParam int count,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        ReservationPost post = reservationPostService.findByIdOrThrow(postId);

        model.addAttribute("post", post);
        model.addAttribute("date", date);
        model.addAttribute("count", count);
        model.addAttribute("userId", userDetails.getUser().getId());

        return "reservation_page/reservation_payment";
    }

    @PostMapping("/payment/confirm")
    public String confirmPayment(@ModelAttribute PaymentConfirmDto dto) {
        ReservationOrderEntity order = reservationOrderService.createOrder(dto.toReservationOrderRequestDto());
        return "redirect:/reservation/complete";
    }

    @GetMapping("/reservation/complete")
    public String reservationCompletePage() {
        return "reservation_page/reservation_complete";
    }

    @GetMapping("/payment/verify")
    public String verifyPayment(@RequestParam("imp_uid") String impUid,
                                @RequestParam("merchant_uid") String merchantUid,
                                @RequestParam("postId") Long postId,
                                @RequestParam("userId") Long userId,
                                @RequestParam("date") String date,
                                @RequestParam("count") int count) {

        try {
            // 1. 포트원 access token 요청
            RestTemplate restTemplate = new RestTemplate();
            String tokenUrl = "https://api.iamport.kr/users/getToken";

            Map<String, String> body = new HashMap<>();
            body.put("imp_key", "6834630365803373");
            body.put("imp_secret", "OBS8wLkT9Rb5p09wjMyJXm0LbAZxinz5CaQGSgIJ5ls7Kfnfs1DTmxRpAvErOBcDvUFaYYIXiepK4RBt");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);
            Map responseMap = (Map) tokenResponse.getBody().get("response");

            if (responseMap == null || responseMap.get("access_token") == null) {
                throw new IllegalStateException("Access token 발급 실패");
            }

            String accessToken = (String) responseMap.get("access_token");

            // 2. imp_uid로 결제 정보 조회
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.set("Authorization", accessToken);
            HttpEntity<Void> authEntity = new HttpEntity<>(authHeaders);

            String paymentUrl = "https://api.iamport.kr/payments/" + impUid;
            ResponseEntity<Map> paymentResponse = restTemplate.exchange(paymentUrl, HttpMethod.GET, authEntity, Map.class);

            Map paymentData = (Map) paymentResponse.getBody().get("response");

            int amountPaid = (int) paymentData.get("amount");
            String status = (String) paymentData.get("status");

            ReservationPost post = reservationPostService.findByIdOrThrow(postId);
            int expected = post.getPrice() * count;

            if (!"paid".equals(status) || amountPaid != expected) {
                throw new IllegalStateException("결제 검증 실패: 금액 불일치 또는 미결제 상태");
            }

            reservationOrderService.createOrder(
                    new PaymentConfirmDto(postId, userId, date, count, true).toReservationOrderRequestDto()
            );

            return "redirect:/reservation/complete";

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("결제 검증 중 오류 발생: " + e.getMessage());
        }
    }
}