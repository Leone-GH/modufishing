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

    // 지역 정보 (부모 포함)
    private List<RegionDto> regions;

    private String companyName;
    private String type;        // ENUM 이름
    private String typeLower;   // 소문자
    private String typeKorean;  // 한글명
    private Integer price;
    private String content;

    // 낚시 종류
    private List<String> fishTypes;

    // 날짜별 예약 정보 (예약자 포함)
    private List<AvailableDateDto> availableDates;

    // ✅ 지역을 "(부모)자식 자식, (부모2)자식 자식" 형식으로 출력
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
        private String rawDate;     // yyyy-MM-dd (전송용)
        private String date;        // yyyy-MM-dd(요일) (출력용)
        private String time;        // 예: 06:00~14:00
        private Integer capacity;   // 정원
        private Integer remaining;  // 남은 자리
        private List<ReservationOrderResponseDto> orders; // ✅ 예약자 목록 추가
    }

    @Getter
    @Builder
    public static class RegionDto {
        private String name;
        private String parentName;
    }

    // 요일 포함 날짜 포맷 함수
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
     * ✅ ReservationPost + 날짜별 예약 정보 + 예약자 목록 → DTO 생성
     * @param post              예약글 Entity
     * @param dateDtos          날짜 정보 리스트
     * @param ordersByDateMap   LocalDate 기준으로 예약자 리스트가 담긴 맵
     */
    public static ReservationDetailResponseDto from(
            ReservationPost post,
            List<AvailableDateDto> dateDtos,
            Map<LocalDate, List<ReservationOrderResponseDto>> ordersByDateMap
    ) {
        // 지역 매핑
        List<RegionDto> regionDtos = post.getRegions().stream()
                .map(region -> RegionDto.builder()
                        .name(region.getName())
                        .parentName(region.getParent() != null ? region.getParent().getName() : null)
                        .build())
                .collect(Collectors.toList());

        // 날짜 + 예약자 정보 합쳐서 재매핑
        List<AvailableDateDto> formattedDateDtos = dateDtos.stream()
                .map(dto -> {
                    LocalDate parsedDate = LocalDate.parse(dto.getDate());  // dto.getDate()는 yyyy-MM-dd
                    return AvailableDateDto.builder()
                            .rawDate(parsedDate.toString())
                            .date(formatDateWithDay(parsedDate))
                            .time(dto.getTime())
                            .capacity(dto.getCapacity())
                            .remaining(dto.getRemaining())
                            .orders(ordersByDateMap.getOrDefault(parsedDate, Collections.emptyList()))
                            .build();
                })
                .collect(Collectors.toList());

        // 최종 DTO 반환
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

    // 오버로딩 버전 (Map 없이도 호출 가능하도록)
    public static ReservationDetailResponseDto from(
            ReservationPost post,
            List<AvailableDateDto> dateDtos
    ) {
        List<RegionDto> regionDtos = post.getRegions().stream()
                .map(region -> RegionDto.builder()
                        .name(region.getName())
                        .parentName(region.getParent() != null ? region.getParent().getName() : null)
                        .build())
                .toList();

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
                                .toList()
                )
                .availableDates(dateDtos)  // 이미 orders 포함된 dto
                .build();
    }

}
