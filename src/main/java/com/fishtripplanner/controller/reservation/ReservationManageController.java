package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.ReservationPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationManageController {

    private final ReservationPostService reservationPostService;

    @GetMapping("/manage")
    @PreAuthorize("hasRole('OWNER')")
    public String showManagePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        model.addAttribute("posts", reservationPostService.getPostsByOwner(userId)); // 이거는 서비스에서 구현
        return "reservation_page/reservation_manage";
    }
}
