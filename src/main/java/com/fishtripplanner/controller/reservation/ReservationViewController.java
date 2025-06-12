package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationViewController {

    private final ReservationQueryService reservationQueryService;

    /**
     * ✅ 기본 예약 메인 페이지
     */
    @GetMapping("")
    public String reservationPage(Model model) {
        List<ReservationPost> posts = reservationQueryService.findAllPosts();
        model.addAttribute("posts", posts);
        return "reservation";
    }

    /**
     * ✅ 예약 타입별 상세 리스트 페이지 (예: /reservation/boat)
     */
    @GetMapping("/{type}")
    public String reservationDetailPage(@PathVariable("type") String type,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        Model model) {
        ReservationType enumType;
        try {
            enumType = ReservationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "redirect:/error"; // 잘못된 타입 요청 시 에러 페이지로
        }

        model.addAttribute("title", enumType.getKorean());
        model.addAttribute("type", type);

        List<ReservationPost> filteredPosts = reservationQueryService.findByType(enumType);
        List<ReservationCardDto> allCards = filteredPosts.stream()
                .map(ReservationCardDto::from)
                .toList();

        int pageSize = 4;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allCards.size());

        if (start >= allCards.size()) {
            model.addAttribute("cards", new ArrayList<ReservationCardDto>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            return "reservation_page/reservation_list";
        }

        List<ReservationCardDto> pagedCards = allCards.subList(start, end);

        model.addAttribute("cards", pagedCards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allCards.size() / pageSize));

        return "reservation_page/reservation_list";
    }

    /**
     * ✅ 예약 상세 페이지 조회
     */
    @GetMapping("/detail/{id}")
    public String getReservationDetail(@PathVariable("id") Long id, Model model) {
        ReservationDetailResponseDto dto = reservationQueryService.getReservationDetail(id);
        model.addAttribute("reservation", dto);
        return "reservation_page/reservation_detail";
    }

    /**
     * ✅ 지역 목록 조회 API (필터용)
     */
    @RestController
    @RequestMapping("/reservation/api")
    @RequiredArgsConstructor
    public static class FilterApiController {

        private final ReservationQueryService reservationQueryService;

        @GetMapping("/regions")
        public List<String> getRegions() {
            return reservationQueryService.getUsedRegionNames();
        }
    }
}