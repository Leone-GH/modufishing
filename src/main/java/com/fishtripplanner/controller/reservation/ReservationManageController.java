package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.dto.reservation.ReservationCreateRequestDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.ReservationPostService;
import com.fishtripplanner.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationManageController {

    private final ReservationPostService reservationPostService;
    private final ReservationQueryService reservationQueryService;

    // ✅ 예약글 관리 페이지
    @GetMapping("/manage")
    @PreAuthorize("hasRole('OWNER')")
    public String showManagePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();

        // 1. 예약글 조회
        var posts = reservationQueryService.getPostsByOwner(userId);

        // 2. postId → 해당 날짜 예약정보 DTO 리스트 매핑
        Map<Long, List<ReservationDetailResponseDto.AvailableDateDto>> availableDateMap = new HashMap<>();
        for (var post : posts) {
            List<ReservationDetailResponseDto.AvailableDateDto> dateDtos =
                    reservationQueryService.getAvailableDatesWithOrders(post.getId());
            availableDateMap.put(post.getId(), dateDtos);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("availableDateMap", availableDateMap);

        return "reservation_page/reservation_manage";
    }

    // ✅ 예약글 삭제
    @GetMapping("/delete/{postId}")
    @PreAuthorize("hasRole('OWNER')")
    public String deleteReservationPost(@PathVariable Long postId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        reservationPostService.deletePost(postId, userId);
        return "redirect:/reservation/manage";
    }

    // ✅ 예약글 수정 폼 출력
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String showEditForm(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReservationPost post = reservationQueryService.findByIdOrThrow(id);

        // 🔐 권한 체크: 내 글만 수정 가능
        if (!post.getOwner().getId().equals(userDetails.getUser().getId())) {
            throw new AccessDeniedException("수정 권한 없음");
        }

        // 🔁 지역 이름 리스트로 변환 (부모 포함 형식)
        List<String> regionNames = post.getRegions().stream()
                .map(r -> r.getParent() != null ? "(" + r.getParent().getName() + ")" + r.getName() : r.getName())
                .toList();

        // 🎣 어종 이름 리스트로 변환
        List<String> fishNames = post.getFishTypes().stream()
                .map(f -> f.getName())
                .toList();

        // 📅 날짜 정보 → JSON-friendly 형태로 가공
        List<Map<String, Object>> availableDatesJson = post.getAvailableDates().stream()
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", d.getId());
                    map.put("availableDate", d.getAvailableDate().toString());
                    map.put("capacity", d.getCapacity());
                    map.put("time", d.getTime());
                    return map;
                })
                .toList();

        // ✅ 모델에 속성 추가
        model.addAttribute("reservationPost", post);
        model.addAttribute("regionNames", regionNames);
        model.addAttribute("fishNames", fishNames);
        model.addAttribute("allRegions", reservationQueryService.getAllRegions());
        model.addAttribute("allFishTypes", reservationQueryService.getAllFishTypes());
        model.addAttribute("availableDatesJson", availableDatesJson); // 🔥 여기가 핵심

        return "reservation_page/reservation_form_edit";
    }

    // ✅ 예약글 수정 처리
    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String updateReservationPost(@PathVariable Long id,
                                        @ModelAttribute ReservationCreateRequestDto dto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        reservationPostService.updateReservation(id, dto, userId);
        return "redirect:/reservation/manage";
    }
}