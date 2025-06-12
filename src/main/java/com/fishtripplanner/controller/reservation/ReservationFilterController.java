package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.RegionDto;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationFilterController {

    private final RegionRepository regionRepository;
    private final ReservationQueryService reservationQueryService;

    /**
     * ✅ 지역 계층 구조 조회
     * - 부모-자식 관계를 갖는 지역 리스트 반환
     * - 지역 선택 모달에 사용
     */
    @GetMapping("/regions/hierarchy")
    public List<RegionDto> getRegionHierarchy() {
        return regionRepository.findAllWithChildrenOnly()
                .stream()
                .map(RegionDto::from)
                .toList();
    }

    /**
     * ✅ 등록된 어종 이름 목록 조회
     * - 어종 선택 모달에 사용
     */
    @GetMapping("/fish-types")
    public List<String> getFishTypes() {
        return reservationQueryService.getFishTypeNames();
    }

    /**
     * ✅ 예약글 필터링 API
     * - type(필수) + regionId/date/fishType/keyword(선택)
     * - 필터 조합에 따라 ReservationPost 목록 반환
     */
    @GetMapping("/reservation")
    public List<ReservationCardDto> getFilteredCards(
            @RequestParam("type") String type,
            @RequestParam(value = "regionId", required = false) List<Long> regionIds,
            @RequestParam(value = "date", required = false) List<String> dateList,
            @RequestParam(value = "fishType", required = false) List<String> fishTypes,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", defaultValue = "latest") String sortKey,
            Pageable pageable
    ) {
        ReservationType enumType = ReservationType.valueOf(type.toUpperCase());

        List<LocalDate> parsedDates = (dateList != null)
                ? dateList.stream().map(LocalDate::parse).toList()
                : null;

        Page<ReservationPost> page = reservationQueryService.filterPosts(
                enumType, regionIds, parsedDates, fishTypes, keyword, sortKey, pageable
        );

        return page.stream()
                .map(ReservationCardDto::from)
                .toList();
    }

    /**
     * ✅ 실제 사용된 지역 이름 목록 반환
     * - 지역 필터용
     */
    @GetMapping("/regions/names")
    public List<String> getUsedRegionNames() {
        return reservationQueryService.getUsedRegionNames();
    }
}