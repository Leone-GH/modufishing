package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.repository.FishTypeRepository;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.ReservationPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationFormController {

    private final ReservationPostService reservationPostService;
    private final RegionRepository regionRepository;        // ✅ 추가
    private final FishTypeRepository fishTypeRepository;    // ✅ 추가

    // ✅ 폼 출력용 GET
    @GetMapping("/write")
    public String showForm(Model model) {
        model.addAttribute("form", new ReservationPostRequest());
        model.addAttribute("regions", regionRepository.findAll());
        model.addAttribute("fishTypes", fishTypeRepository.findAll());
        return "reservation_page/reservationForm";
    }

    // ✅ 폼 제출 POST
    @PostMapping("/new")
    public String submitForm(@ModelAttribute ReservationPostRequest request,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        reservationPostService.createReservationPost(request, userDetails.getUser());
        return "redirect:/reservation/" + request.getType().toLowerCase();
    }
}
