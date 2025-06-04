package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.api.reservation.ReservationService;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.domain.reservation.mapper.ReservationTypeMapper;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.repository.ReservationPostRepository;
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

    private final ReservationPostRepository reservationPostRepository;
    private final ReservationService reservationService;

    // 기본 예약 진입 페이지
    @GetMapping("")
    public String reservationPage(Model model) {
        List<ReservationPost> posts = reservationPostRepository.findAll();
        model.addAttribute("posts", posts);
        return "reservation";
    }

    // 예약 타입별 상세 페이지 (예: /reservation/boat)
    @GetMapping("/{type}")
    public String reservationDetailPage(@PathVariable("type") String type,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        Model model) {
        ReservationType enumType;
        try {
            enumType = ReservationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "redirect:/error";
        }

        String koreanTitle = ReservationTypeMapper.toKorean(enumType); // ✅ 깔끔하게 변경됨
        model.addAttribute("title", koreanTitle);
        model.addAttribute("type", type); // 페이징 링크에 사용됨

        // 실제 예약 카드 생성
        List<ReservationPost> filteredPosts = reservationPostRepository.findByType(enumType);
        List<Object> allCards = new ArrayList<>();
        for (ReservationPost post : filteredPosts) {
            allCards.add(ReservationCardDto.from(post));
        }

        // 페이징 처리
        int pageSize = 4;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allCards.size());

        // ✅ 잘못된 범위 예외 방지
        if (start >= allCards.size()) {
            model.addAttribute("cards", new ArrayList<ReservationCardDto>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            return "reservation_page/reservation_list";
        }

        List<Object> pagedCards = allCards.subList(start, end);

        // 모델에 카드 리스트 추가
        model.addAttribute("cards", pagedCards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allCards.size() / pageSize));

        return "reservation_page/reservation_list";
    }


    // 지역 필터용 API
    @RestController
    @RequestMapping("/api")
    public class FilterApiController {

        private final ReservationPostRepository repo;

        public FilterApiController(ReservationPostRepository repo) {
            this.repo = repo;
        }

        @GetMapping("/regions")
        public List<String> getRegions() {
            return repo.findAllRegionNames();  // ✅ 올바른 메서드 이름
        }
    }

    @GetMapping("/detail/{id}")
    public String getReservationDetail(@PathVariable("id") Long id, Model model) {
        ReservationDetailResponseDto dto = reservationService.getReservationDetail(id);
        model.addAttribute("reservation", dto);
        return "reservation_page/reservation_detail";
    }



}
