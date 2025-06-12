package com.fishtripplanner.service;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.dto.reservation.ReservationOrderResponseDto;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.entity.ReservationOrderEntity;
import com.fishtripplanner.repository.FishTypeRepository;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.repository.ReservationOrderRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationQueryService {

    private final ReservationPostRepository reservationPostRepository;
    private final ReservationOrderRepository reservationOrderRepository;

    private final RegionRepository regionRepository;
    private final FishTypeRepository fishTypeRepository;

    /**
     * 예약글 상세 정보 + 날짜별 예약자 포함
     */
    public ReservationDetailResponseDto getReservationDetail(Long postId) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("예약글 없음"));

        var dateDtos = getAvailableDatesWithOrders(postId);
        return ReservationDetailResponseDto.from(post, dateDtos);
    }

    /**
     * 날짜별 예약자 리스트 포함 DTO
     */
    public List<ReservationDetailResponseDto.AvailableDateDto> getAvailableDatesWithOrders(Long postId) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("예약글 없음"));

        Map<LocalDate, Integer> capMap = post.getAvailableDates().stream()
                .collect(Collectors.toMap(
                        ReservationPostAvailableDate::getAvailableDate,
                        ReservationPostAvailableDate::getCapacity));

        Map<LocalDate, List<ReservationOrderEntity>> ordMap =
                reservationOrderRepository.findAllByReservationPost_Id(postId).stream()
                        .collect(Collectors.groupingBy(ReservationOrderEntity::getReservationDate));

        return post.getAvailableDates().stream().map(ad -> {
            LocalDate date = ad.getAvailableDate();
            int cap = capMap.getOrDefault(date, 0);
            int paid = Optional.ofNullable(reservationOrderRepository
                    .sumPaidCountByPostIdAndDate(postId, date)).orElse(0);

            List<ReservationOrderResponseDto> dtoOrders = ordMap
                    .getOrDefault(date, List.of())
                    .stream()
                    .map(o -> ReservationOrderResponseDto.builder()
                            .id(o.getId())
                            .userNick(o.getUser().getName())
                            .postTitle(post.getTitle())
                            .reservationDate(o.getReservationDate())
                            .serviceTime(o.getServiceTime())
                            .count(o.getCount())
                            .paid(o.isPaid())
                            .createdAt(o.getCreatedAt())
                            .totalCapacity(cap)
                            .reservedCount(paid)
                            .remainingCapacity(cap - paid)
                            .build())
                    .toList();

            return ReservationDetailResponseDto.AvailableDateDto.builder()
                    .rawDate(date.toString())
                    .date(date.toString())
                    .time(ad.getTime())
                    .capacity(cap)
                    .remaining(cap - paid)
                    .orders(dtoOrders)
                    .build();
        }).toList();
    }

    /**
     * 예약글 목록 (사업자 기준)
     */
    public List<ReservationPost> getPostsByOwner(Long userId) {
        return reservationPostRepository.findAllByOwner_Id(userId);
    }

    /**
     * 예약글별 예약 주문 요약
     */
    public List<ReservationOrderResponseDto> getOrderSummariesByPostId(Long postId) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("예약글 없음"));

        var cap = post.getAvailableDates().stream()
                .collect(Collectors.toMap(
                        ReservationPostAvailableDate::getAvailableDate,
                        ReservationPostAvailableDate::getCapacity));
        var paidMap = reservationOrderRepository.findPaidCountsByPostGroupedByDate(postId);

        return post.getReservationOrders().stream()
                .map(o -> {
                    LocalDate d = o.getReservationDate();
                    int capTot = cap.getOrDefault(d, 0);
                    int paidCnt = paidMap.getOrDefault(d, 0);
                    return ReservationOrderResponseDto.builder()
                            .id(o.getId())
                            .userNick(o.getUser().getName())
                            .postTitle(post.getTitle())
                            .reservationDate(d)
                            .serviceTime(o.getServiceTime())
                            .count(o.getCount())
                            .paid(o.isPaid())
                            .totalCapacity(capTot)
                            .reservedCount(paidCnt)
                            .remainingCapacity(capTot - paidCnt)
                            .build();
                }).toList();
    }

    /**
     * 예약글 조건 검색 (유형/지역/날짜/어종/키워드/정렬)
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
        List<Long> regionsSafe = (regionIds == null || regionIds.isEmpty()) ? null : regionIds;
        List<String> fishTypesSafe = (fishTypes == null || fishTypes.isEmpty()) ? null : fishTypes;
        String keywordSafe = (keyword == null || keyword.isBlank()) ? null : keyword;

        Sort sort = switch (sortKey) {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "latest" -> Sort.by("createdAt").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        String key = String.format("%s-%s-%s",
                regionsSafe != null, dates != null, fishTypesSafe != null);

        return switch(key) {
            case "true-true-true" -> reservationPostRepository.findByFiltersStrict(type, regionsSafe, dates, fishTypesSafe, sortedPageable);
            case "true-true-false" -> reservationPostRepository.findByTypeAndRegionIdsAndDate(type, regionsSafe, dates, sortedPageable);
            case "false-true-true" -> reservationPostRepository.findByDateAndFishTypes(type, dates, fishTypesSafe, sortedPageable);
            case "true-false-true" -> reservationPostRepository.findByRegionIdsAndFishTypes(type, regionsSafe, fishTypesSafe, sortedPageable);
            case "false-false-true" -> reservationPostRepository.findByFishTypes(type, fishTypesSafe, sortedPageable);
            case "false-true-false" -> reservationPostRepository.findByTypeAndDate(type, dates, sortedPageable);
            case "true-false-false" -> reservationPostRepository.findByTypeAndRegionIds(type, regionsSafe, sortedPageable);
            default -> {
                if(regionsSafe == null && dates == null && fishTypesSafe == null && keywordSafe == null)
                    yield reservationPostRepository.findByType(type, sortedPageable);
                yield reservationPostRepository.findByFilters(type, regionsSafe, dates, fishTypesSafe, keywordSafe, sortedPageable);
            }
        };
    }

    /**
     * 어종 이름 목록
     */
    public List<String> getFishTypeNames() {
        return reservationPostRepository.findAllFishTypeNames().stream().sorted().toList();
    }

    /**
     * 사용된 지역 이름 목록
     */
    public List<String> getUsedRegionNames() {
        return reservationPostRepository.findAllRegionNames();
    }

    public ReservationPost findByIdOrThrow(Long id) {
        return reservationPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약글이 존재하지 않습니다."));
    }

    public List<RegionEntity> getAllRegions() {
        return regionRepository.findAll();
    }

    public List<FishTypeEntity> getAllFishTypes() {
        return fishTypeRepository.findAll();
    }

    public List<ReservationPost> findAllPosts() {
        return reservationPostRepository.findAll();
    }

    public List<ReservationPost> findByType(ReservationType type) {
        return reservationPostRepository.findByType(type);
    }
}