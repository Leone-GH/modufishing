package com.fishtripplanner.api.reservation;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.*;
import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.dto.ReservationPostResponse;
import com.fishtripplanner.dto.reservation.CreateReservationRequestDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.dto.reservation.ReservationResponseDto;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationPostRepository reservationPostRepository;
    private final ReservationRequestRepository reservationRequestRepository;
    private final ReservationOrderRepository reservationOrderRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final FishTypeRepository fishTypeRepository;

    /**
     * 여러 지역을 하나의 예약글에 추가하여 예약글 생성
     */
    public List<ReservationPostResponse> createReservationPosts(ReservationPostRequest request, User user) {
        List<RegionEntity> regions = regionRepository.findAllById(request.getRegionIds());
        List<FishTypeEntity> fishTypes = fishTypeRepository.findAllById(request.getFishTypeIds());

        if (regions.isEmpty() || fishTypes.isEmpty()) {
            return List.of(); // 예외 처리 없이 비어있으면 반환
        }

        ReservationPost post = ReservationPost.builder()
                .owner(user)
                .type(ReservationType.valueOf(request.getType()))
                .title(request.getTitle())
                .content(request.getContent())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .regions(regions)
                .build();

        post.setFishTypes(fishTypes);

        List<ReservationPostAvailableDate> availableDateList = request.getAvailableDates().stream()
                .map(date -> ReservationPostAvailableDate.builder()
                        .availableDate(date)
                        .reservationPost(post)
                        .build())
                .collect(Collectors.toList());

        post.setAvailableDates(availableDateList);

        ReservationPost savedPost = reservationPostRepository.save(post);
        return List.of(ReservationPostResponse.from(savedPost));
    }

    public List<ReservationPostResponse> getAllPosts() {
        return reservationPostRepository.findAll().stream()
                .map(ReservationPostResponse::from)
                .collect(Collectors.toList());
    }

    public ReservationResponseDto createReservationRequest(CreateReservationRequestDto request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        ReservationPost post = reservationPostRepository.findById(request.getPostId()).orElseThrow();

        ReservationRequest reservationRequest = ReservationRequest.builder()
                .user(user)
                .reservationPost(post)
                .reservedDate(request.getReservedDate())
                .message(request.getMessage())
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return ReservationResponseDto.from(reservationRequestRepository.save(reservationRequest));
    }

    public List<ReservationResponseDto> getRequestsForPost(Long postId) {
        return reservationRequestRepository.findByReservationPostId(postId).stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    public ReservationResponseDto updateRequestStatus(Long requestId, ReservationStatus status) {
        ReservationRequest request = reservationRequestRepository.findById(requestId).orElseThrow();
        request.setStatus(status);
        return ReservationResponseDto.from(reservationRequestRepository.save(request));
    }

    public List<ReservationPostResponse> getPostsByOwner(Long ownerId) {
        return reservationPostRepository.findByOwnerId(ownerId).stream()
                .map(ReservationPostResponse::from)
                .collect(Collectors.toList());
    }

    public List<ReservationResponseDto> getRequestsByUser(Long userId) {
        return reservationRequestRepository.findByUserId(userId).stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    public Page<ReservationPost> getPostsByRegion(String type, List<Long> regionIds, Pageable pageable) {
        ReservationType reservationType = ReservationType.valueOf(type.toUpperCase());
        if (regionIds != null && !regionIds.isEmpty()) {
            return reservationPostRepository.findByTypeAndRegionIds(reservationType, regionIds, pageable);
        } else {
            return reservationPostRepository.findByType(reservationType, pageable);
        }
    }

    @Transactional(readOnly = true)
    public ReservationDetailResponseDto getReservationDetail(Long id) {
        ReservationPost post = reservationPostRepository.findByIdWithAvailableDatesOnly(id)
                .orElseThrow(() -> new RuntimeException("예약글을 찾을 수 없습니다."));

        // availableDates 계산만 남기고 regionNames는 DTO에서 처리함
        List<ReservationDetailResponseDto.AvailableDateDto> availableDates = post.getAvailableDates().stream()
                .map(date -> {
                    // countByReservationPostAndAvailableDate를 sumCountByPostIdAndDate로 변경
                    Integer reserved = reservationOrderRepository.sumCountByPostIdAndDate(post.getId(), date.getAvailableDate());
                    if (reserved == null) reserved = 0; // 예약된 인원이 없으면 0으로 처리

                    return ReservationDetailResponseDto.AvailableDateDto.builder()
                            .date(date.getAvailableDate().toString())
                            .time(date.getTime())
                            .capacity(date.getCapacity())
                            .remaining(date.getCapacity() - reserved)  // 남은 좌석 수 계산
                            .build();
                })
                .collect(Collectors.toList());

        return ReservationDetailResponseDto.from(post, availableDates);
    }


}
