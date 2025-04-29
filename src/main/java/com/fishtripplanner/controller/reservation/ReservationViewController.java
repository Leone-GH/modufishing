package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationViewController {

    private final ReservationPostRepository reservationPostRepository;

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

        String koreanTitle = switch (type.toLowerCase()) {
            case "boat" -> "선상낚시";
            case "stay" -> "숙박/민박/캠핑";
            case "float" -> "좌대";
            case "island" -> "섬";
            case "rock" -> "갯바위";
            default -> "낚시 예약";
        };
        model.addAttribute("title", koreanTitle);
        model.addAttribute("type", type); // 페이징 링크에 사용됨

        ReservationType enumType;
        try {
            enumType = ReservationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "redirect:/error";
        }

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
        List<Object> pagedCards = allCards.subList(start, end);

        // 모델에 카드 리스트 추가
        model.addAttribute("cards", pagedCards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allCards.size() / pageSize));

        return "reservation_detail/reservation_detail";
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
            return repo.findAllRegions(); // 지역 이름 목록만 반환
        }
    }
}
