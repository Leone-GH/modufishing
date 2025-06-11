package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.dto.reservation.ReservationCreateRequestDto;
import com.fishtripplanner.repository.BusinessInfoRepository;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.repository.FishTypeRepository;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.ReservationPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationFormController {

    private final ReservationPostService reservationPostService;
    private final RegionRepository regionRepository;
    private final FishTypeRepository fishTypeRepository;
    private final BusinessInfoRepository businessInfoRepository;

    // ✅ 예약글 작성 폼 페이지 출력
    @GetMapping("/form")
    public String showForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("form", new ReservationCreateRequestDto());
        model.addAttribute("regions", regionRepository.findAll());
        model.addAttribute("fishTypes", fishTypeRepository.findAll());

        Long userId = userDetails.getUser().getId();
        String companyName = businessInfoRepository.findCompanyNameByUserId(userId)
                .orElse("")
                .trim();

        model.addAttribute("companyName", companyName);

        return "reservation_page/reservation_form";
    }


    // ✅ 예약글 작성 처리 후 메인으로 리디렉션 + 알림 메시지 전달
    @PostMapping("/new")
    public String createReservation(@ModelAttribute ReservationCreateRequestDto formDto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        formDto.setUserId(userDetails.getUser().getId()); // ✅ 작성자 ID 주입
        reservationPostService.saveReservation(formDto);
        redirectAttributes.addFlashAttribute("successMessage", "예약글이 성공적으로 등록되었습니다.");
        return "redirect:/";
    }
}
