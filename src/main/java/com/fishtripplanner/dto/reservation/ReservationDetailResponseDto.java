package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.entity.FishTypeEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReservationDetailResponseDto {

    private Long id;
    private String title;
    private String imageUrl;

    // ✅ 지역 이름들을 리스트로 응답
    private List<String> regionNames;

    private String companyName;
    private String type;        // ENUM 이름 (ex: FISHING)
    private String typeLower;   // ENUM 소문자 (ex: fishing)
    private String typeKorean;  // ENUM 한글명 (ex: 일반 낚시)
    private Integer price;
    private String content;

    // 🎣 낚시 종류
    private List<String> fishTypes;

    // 📆 날짜별 예약 가능 정보
    private List<AvailableDateDto> availableDates;

    @Getter
    @Builder
    public static class AvailableDateDto {
        private String date;       // 예약일 (yyyy-MM-dd)
        private String time;       // 예약 시간대 (예: 06:00~14:00)
        private Integer capacity;  // 최대 정원
        private Integer remaining; // 남은 인원 수
    }

    /**
     * ReservationPost + 예약 가능 날짜 리스트 → DTO 변환
     */
    public static ReservationDetailResponseDto from(ReservationPost post, List<AvailableDateDto> availableDates) {

        // ✅ 여러 지역을 (서울) 강남 형태로 표시해서 리스트로 구성
        List<String> regionNames = post.getRegions().stream()
                .map(region -> {
                    String parent = region.getParent() != null ? "(" + region.getParent().getName() + ") " : "";
                    return parent + region.getName();
                })
                .collect(Collectors.toList());

        return ReservationDetailResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .imageUrl(
                        post.getImageUrl() != null && !post.getImageUrl().isEmpty()
                                ? post.getImageUrl()
                                : "/images/" + post.getType().name().toLowerCase() + ".jpg"
                )
                .regionNames(regionNames)  // ✅ 리스트로 설정
                .companyName(post.getCompanyName())
                .type(post.getType().name())
                .typeLower(post.getType().name().toLowerCase())
                .typeKorean(post.getType().getKorean())
                .price(post.getPrice())
                .content(post.getContent())
                .fishTypes(
                        post.getFishTypes().stream()
                                .map(FishTypeEntity::getName)
                                .collect(Collectors.toList())
                )
                .availableDates(availableDates)
                .build();
    }
}
