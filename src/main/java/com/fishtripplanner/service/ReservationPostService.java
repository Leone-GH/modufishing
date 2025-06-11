package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.dto.ReservationPostResponse;
import com.fishtripplanner.dto.reservation.ReservationCreateRequestDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.dto.reservation.ReservationOrderResponseDto;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.entity.ReservationOrderEntity;
import com.fishtripplanner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationPostService {

    private final ReservationPostRepository reservationPostRepository;
    private final RegionRepository regionRepository;
    private final FishTypeRepository fishTypeRepository;
    private final ReservationPostAvailableDateRepository availableDateRepository;
    private final UserRepository userRepository;
    private final ReservationOrderRepository reservationOrderRepository;

    /**
     * ✅ 예약글 등록
     */
    public List<ReservationPostResponse> createReservationPosts(ReservationPostRequest request, User user) {
        List<RegionEntity> regions = regionRepository.findAllById(request.getRegionIds());
        List<FishTypeEntity> fishTypes = fishTypeRepository.findAllById(request.getFishTypeIds());

        ReservationPost post = request.toEntity(regions);
        post.setOwner(user);
        post.setFishTypes(fishTypes);
        reservationPostRepository.save(post);

        List<ReservationPostAvailableDate> availableDates = request.getAvailableDates().stream()
                .map(d -> ReservationPostAvailableDate.builder()
                        .reservationPost(post)
                        .availableDate(d)
                        .build())
                .toList();
        availableDateRepository.saveAll(availableDates);

        return List.of(ReservationPostResponse.from(post));
    }

    /**
     * ✅ 조건별 예약글 필터링
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

    public List<String> getFishTypeNames() {
        return reservationPostRepository.findAllFishTypeNames().stream().sorted().toList();
    }

    public List<String> getUsedRegionNames() {
        return reservationPostRepository.findAllRegionNames();
    }

    /**
     * ✅ 단일 예약글 저장 (이미지 포함)
     */
    public void saveReservation(ReservationCreateRequestDto dto) {
        if(dto.getUserId() == null || dto.getRegionIds().isEmpty()
                || dto.getFishTypeNames().isEmpty()
                || dto.getAvailableDates().isEmpty()) {
            throw new IllegalArgumentException("필수 입력 값 누락");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        List<RegionEntity> regions = regionRepository.findAllById(dto.getRegionIds());
        List<FishTypeEntity> fishTypes = fishTypeRepository.findByNameIn(dto.getFishTypeNames());

        ReservationPost post = new ReservationPost();
        post.setTitle(dto.getTitle());
        post.setType(ReservationType.valueOf(dto.getType()));
        post.setRegionAndContentAndPriceAndCompany(
                regions, dto.getContent(), dto.getPrice(), dto.getCompanyName());
        post.setOwner(user);
        post.setFishTypes(fishTypes);

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String fname = UUID.randomUUID() + "_" + dto.getImageFile().getOriginalFilename();
            Path up = Paths.get("uploads/reservation_images/", fname);
            try {
                Files.createDirectories(up.getParent());
                Files.write(up, dto.getImageFile().getBytes());
                post.setImageUrl("/uploads/reservation_images/" + fname);
            } catch(IOException e){
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

        reservationPostRepository.save(post);
        availableDateRepository.saveAll(dto.getAvailableDates().stream()
                .map(d -> ReservationPostAvailableDate.builder()
                        .reservationPost(post)
                        .availableDate(LocalDate.parse(d.getDate()))
                        .capacity(d.getCapacity())
                        .time(d.getTime())
                        .build())
                .toList());
    }

    /**
     * ✅ 예약글 상세조회 + 날짜별 예약자 포함
     */
    public ReservationDetailResponseDto getReservationDetail(Long postId) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("예약글 없음"));

        var dateDtos = getAvailableDatesWithOrders(postId);
        return ReservationDetailResponseDto.from(post, dateDtos);
    }

    /**
     * ✅ 날짜별 예약정보 + 예약자 리스트 포함 DTO
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


    public List<ReservationPost> getPostsByOwner(Long userId) {
        return reservationPostRepository.findAllByOwner_Id(userId);
    }

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
}
