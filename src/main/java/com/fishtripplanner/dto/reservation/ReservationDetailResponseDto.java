package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.entity.FishTypeEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReservationDetailResponseDto {

    private Long id;
    private String title;
    private String imageUrl;

    // ✅ 지역을 이름 + 부모로 묶은 RegionDto 리스트로 변경
    private List<RegionDto> regions;

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

    /**
     * ✅ 지역을 "(부모)자식 자식, (부모2)자식 자식" 형식으로 포맷팅한 문자열 반환
     */
    public String getFormattedRegionString() {
        if (regions == null || regions.isEmpty()) return "";

        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (RegionDto r : regions) {
            String parent = r.getParentName() != null ? r.getParentName() : "기타";
            grouped.computeIfAbsent(parent, k -> new ArrayList<>()).add(r.getName());
        }

        return grouped.entrySet().stream()
                .map(e -> "(" + e.getKey() + ")" + String.join(" ", e.getValue()))
                .collect(Collectors.joining(", "));
    }

    @Getter
    @Builder
    public static class AvailableDateDto {
        private String rawDate;     // ✅ 전송용: yyyy-MM-dd (서버 전달 시 사용)
        private String date;        // ✅ 출력용: yyyy-MM-dd(요일) (화면 표시용)
        private String time;        // 예약 시간대 (예: 06:00~14:00)
        private Integer capacity;   // 최대 정원
        private Integer remaining;  // 남은 인원 수
    }

    @Getter
    @Builder
    public static class RegionDto {
        private String name;
        private String parentName;
    }

    /**
     * ✅ 요일 포함한 날짜 포맷 함수
     */
    private static String formatDateWithDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String dayKor = switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
        return date.toString() + "(" + dayKor + ")";
    }

    /**
     * ✅ ReservationPost + 예약 가능 날짜 리스트 → DTO 변환
     */
    public static ReservationDetailResponseDto from(ReservationPost post, List<AvailableDateDto> dateDtos) {

        // ✅ Region → RegionDto로 매핑
        List<RegionDto> regionDtos = post.getRegions().stream()
                .map(region -> RegionDto.builder()
                        .name(region.getName())
                        .parentName(region.getParent() != null ? region.getParent().getName() : null)
                        .build())
                .collect(Collectors.toList());

        // ✅ 날짜 포맷
        List<AvailableDateDto> formattedDateDtos = dateDtos.stream()
                .map(dto -> {
                    LocalDate parsedDate = LocalDate.parse(dto.getDate());
                    return AvailableDateDto.builder()
                            .rawDate(parsedDate.toString())
                            .date(formatDateWithDay(parsedDate))
                            .time(dto.getTime())
                            .capacity(dto.getCapacity())
                            .remaining(dto.getRemaining())
                            .build();
                })
                .collect(Collectors.toList());

        // ✅ 최종 DTO 생성
        return ReservationDetailResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .imageUrl(
                        post.getImageUrl() != null && !post.getImageUrl().isEmpty()
                                ? post.getImageUrl()
                                : "/images/" + post.getType().name().toLowerCase() + ".jpg"
                )
                .regions(regionDtos)
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
                .availableDates(formattedDateDtos)
                .build();
    }

}
