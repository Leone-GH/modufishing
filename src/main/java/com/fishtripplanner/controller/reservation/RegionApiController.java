package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.RegionDto;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegionApiController {

    private final RegionRepository regionRepository;
    private final ReservationPostRepository reservationPostRepository;

    // ✅ 지역 계층 구조 반환
    @GetMapping("/regions/hierarchy")
    public List<RegionDto> getRegionHierarchy() {
        return regionRepository.findAllWithChildrenOnly()
                .stream()
                .map(RegionDto::from)
                .toList();
    }

    // ✅ 지역 필터링 포함한 예약 카드 조회 API
    @GetMapping("/reservation")
    public List<ReservationCardDto> getFilteredCards(
            @RequestParam(name = "type") String type,
            @RequestParam(name = "regionId", required = false) List<Long> regionIds,
            Pageable pageable
    ) {
        // 예약 타입 enum 처리
        ReservationType enumType;
        try {
            enumType = ReservationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 예약 타입입니다: " + type);
        }

        // 필터링 쿼리 호출
        Page<ReservationPost> page;
        if (regionIds != null && !regionIds.isEmpty()) {
            page = reservationPostRepository.findByTypeAndRegionIds(enumType, regionIds, pageable);
        } else {
            page = reservationPostRepository.findByType(enumType, pageable);
        }

        // DTO 변환
        return page.stream()
                .map(ReservationCardDto::from)
                .toList();
    }
}
