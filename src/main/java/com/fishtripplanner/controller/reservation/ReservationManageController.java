package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.dto.reservation.ReservationCreateRequestDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.dto.reservation.ReservationOrderResponseDto;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.ReservationPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationManageController {

    private final ReservationPostService reservationPostService;

    @GetMapping("/manage")
    @PreAuthorize("hasRole('OWNER')")
    public String showManagePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();

        // 1. 예약글 가져오기
        var posts = reservationPostService.getPostsByOwner(userId);

        // 2. 예약자 리스트 Map<Long postId, List<ReservationOrderResponseDto>> 형태로 구성
        Map<Long, List<ReservationDetailResponseDto.AvailableDateDto>> availableDateMap = new HashMap<>();
        for (var post : posts) {
            List<ReservationDetailResponseDto.AvailableDateDto> dateDtos = reservationPostService.getAvailableDatesWithOrders(post.getId());
            availableDateMap.put(post.getId(), dateDtos);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("availableDateMap", availableDateMap); // ✅ Thymeleaf에서 post.id 기준 접근 가능

        return "reservation_page/reservation_manage";
    }

    @GetMapping("/delete/{postId}")
    @PreAuthorize("hasRole('OWNER')")
    public String deleteReservationPost(@PathVariable Long postId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        reservationPostService.deletePost(postId, userId);
        return "redirect:/reservation/manage";
    }

}
