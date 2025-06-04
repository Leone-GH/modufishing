package com.fishtripplanner.api.reservation;

import com.fishtripplanner.dto.reservation.CreateReservationRequestDto;
import com.fishtripplanner.dto.reservation.ReservationResponseDto;
import com.fishtripplanner.domain.reservation.ReservationStatus;
import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.dto.ReservationPostResponse;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약글 등록 API
    @PostMapping("/register")
    public ResponseEntity<List<ReservationPostResponse>> register(
            @RequestBody ReservationPostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 사용자가 보낸 예약글 요청을 처리하여 여러 지역을 포함한 예약글 생성
        List<ReservationPostResponse> responses = reservationService.createReservationPosts(request, userDetails.getUser());

        // 예약글 등록 후 생성된 예약글 리스트를 응답으로 반환 (201 Created 상태 코드)
        return ResponseEntity.status(201).body(responses);  // 응답 상태는 201 CREATED로 반환
    }

    // 전체 예약글 조회 API
    @GetMapping("/list")
    public ResponseEntity<List<ReservationPostResponse>> getAll() {
        return ResponseEntity.ok(reservationService.getAllPosts());
    }

    // 예약 요청 API (유저가 예약 신청)
    @PostMapping("/request")
    public ResponseEntity<ReservationResponseDto> requestReservation(@RequestBody CreateReservationRequestDto request) {
        return ResponseEntity.ok(reservationService.createReservationRequest(request));
    }

    // 특정 예약글에 대한 모든 예약 요청 조회
    @GetMapping("/requests/{postId}")
    public ResponseEntity<List<ReservationResponseDto>> getRequestsForPost(@PathVariable Long postId) {
        return ResponseEntity.ok(reservationService.getRequestsForPost(postId));
    }

    // 예약 요청 승인 또는 거절 처리
    @PatchMapping("/requests/{requestId}/status")
    public ResponseEntity<ReservationResponseDto> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestParam ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateRequestStatus(requestId, status));
    }

    // 내가 작성한 예약글 조회
    @GetMapping("/my-posts")
    public ResponseEntity<List<ReservationPostResponse>> getMyPosts(@RequestParam Long ownerId) {
        return ResponseEntity.ok(reservationService.getPostsByOwner(ownerId));
    }

    // 내가 신청한 예약 요청 조회
    @GetMapping("/my-requests")
    public ResponseEntity<List<ReservationResponseDto>> getMyRequests(@RequestParam Long userId) {
        return ResponseEntity.ok(reservationService.getRequestsByUser(userId));
    }
}
