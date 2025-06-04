package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.dto.ReservationPostResponse;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationPostService {

    private final ReservationPostRepository reservationPostRepository;
    private final RegionRepository regionRepository;
    private final FishTypeRepository fishTypeRepository;
    private final ReservationPostAvailableDateRepository availableDateRepository;

    public ReservationPostResponse createReservationPost(ReservationPostRequest request, User user) {
        RegionEntity region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 지역 ID"));

        ReservationPost post = request.toEntity(region);
        post.setOwner(user);

        List<FishTypeEntity> fishTypes = fishTypeRepository.findAllById(request.getFishTypeIds());
        post.setFishTypes(fishTypes);

        reservationPostRepository.save(post);

        List<ReservationPostAvailableDate> availableDates = request.getAvailableDates().stream()
                .map(date -> ReservationPostAvailableDate.builder()
                        .reservationPost(post)
                        .availableDate(date)
                        .build())
                .toList();
        availableDateRepository.saveAll(availableDates);

        // ✅ from() 메서드로 생성
        return ReservationPostResponse.from(post);
    }

    /**
     * ✅ 예약글 필터링
     */
    public Page<ReservationPost> filterPosts(
            ReservationType type,
            List<Long> regionIds,
            List<LocalDate> dates,
            List<String> fishTypes,
            String keyword,
            String sortKey,
            Pageable pageable
    ) {
        List<Long> safeRegionIds = (regionIds == null || regionIds.isEmpty()) ? null : regionIds;
        List<String> safeFishTypes = (fishTypes == null || fishTypes.isEmpty()) ? null : fishTypes;
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;

        Sort sort = switch (sortKey) {
            case "priceAsc"  -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "latest"    -> Sort.by("createdAt").descending();
            default          -> Sort.by("createdAt").descending();
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        boolean hasRegion = safeRegionIds != null;
        boolean hasDate = dates != null;
        boolean hasFish = safeFishTypes != null;

        String conditionKey = String.format("%s-%s-%s", hasRegion, hasDate, hasFish);

        return switch (conditionKey) {
            case "true-true-true"   -> reservationPostRepository.findByFiltersStrict(
                    type, safeRegionIds, dates, safeFishTypes, sortedPageable);
            case "true-true-false"  -> reservationPostRepository.findByTypeAndRegionIdsAndDate(
                    type, safeRegionIds, dates, sortedPageable);
            case "false-true-true"  -> reservationPostRepository.findByDateAndFishTypes(
                    type, dates, safeFishTypes, sortedPageable);
            case "true-false-true"  -> reservationPostRepository.findByRegionIdsAndFishTypes(
                    type, safeRegionIds, safeFishTypes, sortedPageable);
            case "false-false-true" -> reservationPostRepository.findByFishTypes(
                    type, safeFishTypes, sortedPageable);
            case "false-true-false" -> reservationPostRepository.findByTypeAndDate(
                    type, dates, sortedPageable);
            case "true-false-false" -> reservationPostRepository.findByTypeAndRegionIds(
                    type, safeRegionIds, sortedPageable);
            default -> {
                if (safeRegionIds == null && dates == null && safeFishTypes == null && safeKeyword == null) {
                    yield reservationPostRepository.findByType(type, sortedPageable);
                }
                yield reservationPostRepository.findByFilters(
                        type, safeRegionIds, dates, safeFishTypes, safeKeyword, sortedPageable);
            }
        };
    }

    public List<String> getFishTypeNames() {
        return reservationPostRepository.findAllFishTypeNames().stream().sorted().toList();
    }

    public List<String> getUsedRegionNames() {
        return reservationPostRepository.findAllRegionNames();
    }
}
