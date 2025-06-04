package com.fishtripplanner.api.reservation;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.*;
import com.fishtripplanner.domain.reservation.mapper.ReservationTypeMapper;
import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.dto.ReservationPostResponse;
import com.fishtripplanner.dto.reservation.CreateReservationRequestDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.dto.reservation.ReservationResponseDto;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import com.fishtripplanner.repository.ReservationRequestRepository;
import com.fishtripplanner.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    /**
     * ✅ 예약글 등록
     */
    public ReservationPostResponse createReservationPost(ReservationPostRequest request, User user) {
        RegionEntity region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다"));

        ReservationPost post = ReservationPost.builder()
                .owner(user) // ⬅️ user 인자로 받음
                .type(ReservationType.valueOf(request.getType()))
                .title(request.getTitle())
                .content(request.getContent())
                .region(region)
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        List<ReservationPostAvailableDate> availableDateList = request.getAvailableDates().stream()
                .map(date -> ReservationPostAvailableDate.builder()
                        .availableDate(date)
                        .reservationPost(post)
                        .build())
                .toList();

        post.setAvailableDates(availableDateList);

        return ReservationPostResponse.from(reservationPostRepository.save(post));
    }

    /**
     * ✅ 모든 예약글 조회
     */
    public List<ReservationPostResponse> getAllPosts() {
        return reservationPostRepository.findAll().stream()
                .map(ReservationPostResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 예약 요청 생성
     */
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

    /**
     * ✅ 특정 예약글에 대한 요청 리스트
     */
    public List<ReservationResponseDto> getRequestsForPost(Long postId) {
        return reservationRequestRepository.findByReservationPostId(postId).stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 예약 요청 상태 변경
     */
    public ReservationResponseDto updateRequestStatus(Long requestId, ReservationStatus status) {
        ReservationRequest request = reservationRequestRepository.findById(requestId).orElseThrow();
        request.setStatus(status);
        return ReservationResponseDto.from(reservationRequestRepository.save(request));
    }

    /**
     * ✅ 내가 작성한 예약글 리스트
     */
    public List<ReservationPostResponse> getPostsByOwner(Long ownerId) {
        return reservationPostRepository.findByOwnerId(ownerId).stream()
                .map(ReservationPostResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 내가 신청한 예약 요청 리스트
     */
    public List<ReservationResponseDto> getRequestsByUser(Long userId) {
        return reservationRequestRepository.findByUserId(userId).stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 지역 필터 기반 예약글 조회
     */
    public Page<ReservationPost> getPostsByRegion(String type, List<Long> regionIds, Pageable pageable) {
        ReservationType reservationType = ReservationType.valueOf(type.toUpperCase());
        if (regionIds != null && !regionIds.isEmpty()) {
            return reservationPostRepository.findByTypeAndRegionIds(reservationType, regionIds, pageable);
        } else {
            return reservationPostRepository.findByType(reservationType, pageable);
        }
    }

    /**
     * ✅ 예약 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public ReservationDetailResponseDto getReservationDetail(Long id) {
        ReservationPost post = reservationPostRepository.findByIdWithAvailableDatesOnly(id)
                .orElseThrow(() -> new RuntimeException("예약글을 찾을 수 없습니다."));

        return ReservationDetailResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .imageUrl(
                        post.getImageUrl() != null && !post.getImageUrl().isEmpty()
                                ? post.getImageUrl()
                                : "/images/" + post.getType().name().toLowerCase() + ".jpg"
                )
                .regionName(post.getRegion().getName())
                .companyName(post.getCompanyName())
                .type(post.getType().name())
                .typeLower(post.getType().name().toLowerCase()) // 👉 이거도 같이 들어가야 템플릿에서 사용 가능
                .typeKorean(ReservationTypeMapper.toKorean(post.getType())) //한국어로 변경
                .price(post.getPrice())
                .content(post.getContent())
                .fishTypes(
                        post.getFishTypes().stream()
                                .map(f -> f.getName())
                                .toList()
                )
                .availableDates(
                        post.getAvailableDates().stream()
                                .map(date -> ReservationDetailResponseDto.AvailableDateDto.builder()
                                        .date(date.getAvailableDate().toString())
                                        .remaining(10) // TODO: 추후 동적으로 처리
                                        .build()
                                ).toList()
                )
                .build();

    }
}
