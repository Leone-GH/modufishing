package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.ReservationCreateRequestDto;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationPostService {

    private final ReservationPostRepository reservationPostRepository;
    private final RegionRepository regionRepository;
    private final FishTypeRepository fishTypeRepository;
    private final ReservationPostAvailableDateRepository availableDateRepository;
    private final UserRepository userRepository;

    /**
     * 예약글 등록
     */
    @Transactional
    public void saveReservation(ReservationCreateRequestDto dto) {
        validateInput(dto);

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
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

        reservationPostRepository.save(post);
        saveAvailableDates(post, dto);
    }

    /**
     * 예약글 수정
     */
    @Transactional
    public void updateReservation(Long postId, ReservationCreateRequestDto dto, Long userId) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("예약글 없음"));

        if (!post.getOwner().getId().equals(userId)) {
            throw new SecurityException("수정 권한 없음");
        }

        List<RegionEntity> regions = regionRepository.findAllById(dto.getRegionIds());
        List<FishTypeEntity> fishTypes = fishTypeRepository.findByNameIn(dto.getFishTypeNames());

        post.setTitle(dto.getTitle());
        post.setType(ReservationType.valueOf(dto.getType()));
        post.setRegionAndContentAndPriceAndCompany(
                regions, dto.getContent(), dto.getPrice(), dto.getCompanyName());
        post.setFishTypes(fishTypes);

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String fname = UUID.randomUUID() + "_" + dto.getImageFile().getOriginalFilename();
            Path up = Paths.get("uploads/reservation_images/", fname);
            try {
                Files.createDirectories(up.getParent());
                Files.write(up, dto.getImageFile().getBytes());
                post.setImageUrl("/uploads/reservation_images/" + fname);
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

        // 기존 날짜 전부 삭제 후 새로 저장
        availableDateRepository.deleteAllByReservationPost(post);
        saveAvailableDates(post, dto);
    }

    /**
     * 예약글 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long ownerId) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약글이 존재하지 않음"));

        if (!post.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("삭제 권한 없음");
        }

        reservationPostRepository.delete(post);
    }

    /**
     * 단건 조회
     */
    public ReservationPost findByIdOrThrow(Long id) {
        return reservationPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약글이 존재하지 않습니다."));
    }

    /**
     * 전체 지역 조회
     */
    public List<RegionEntity> getAllRegions() {
        return regionRepository.findAll();
    }

    /**
     * 전체 어종 조회
     */
    public List<FishTypeEntity> getAllFishTypes() {
        return fishTypeRepository.findAll();
    }

    // ────────────────────────────────

    private void validateInput(ReservationCreateRequestDto dto) {
        if (dto.getUserId() == null || dto.getRegionIds().isEmpty()
                || dto.getFishTypeNames().isEmpty() || dto.getAvailableDates().isEmpty()) {
            throw new IllegalArgumentException("필수 입력 값 누락");
        }
    }

    private void saveAvailableDates(ReservationPost post, ReservationCreateRequestDto dto) {
        var dates = dto.getAvailableDates().stream()
                .map(d -> ReservationPostAvailableDate.builder()
                        .reservationPost(post)
                        .availableDate(LocalDate.parse(d.getDate()))
                        .capacity(d.getCapacity())
                        .time(d.getTime())
                        .build())
                .toList();
        availableDateRepository.saveAll(dates);
    }
}