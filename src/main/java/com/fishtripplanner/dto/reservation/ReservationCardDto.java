package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCardDto {

    private Long id;
    private String title;
    private String content;
    private String companyName;
    private String imageUrl;
    private String region;             // ✅ 여러 지역 문자열로 표시 (ex: (서울)강남구 송파구, (경기)수원시)
    private List<String> fishTypes;
    private String typeKorean;         // ✅ 한글 예약 타입명 (예: 선상, 갯바위, 민박 등)

    /**
     * ReservationPost → ReservationCardDto 변환 로직
     */
    public static ReservationCardDto from(ReservationPost post) {
        // ✅ 지역 문자열 처리
        String regionText = "미지정";
        List<RegionEntity> regions = post.getRegions();
        if (regions != null && !regions.isEmpty()) {
            Map<String, List<String>> grouped = new LinkedHashMap<>();
            for (RegionEntity region : regions) {
                String parentName = region.getParent() != null ? region.getParent().getName() : "기타";
                String childName = region.getName();
                grouped.computeIfAbsent(parentName, k -> new ArrayList<>()).add(childName);
            }
            List<String> formatted = grouped.entrySet().stream()
                    .map(entry -> "(" + entry.getKey() + ")" + String.join(" ", entry.getValue()))
                    .collect(Collectors.toList());
            regionText = String.join(", ", formatted);
        }

        // ✅ 이미지 경로 보정
        String imageUrl = post.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            // 타입별 기본 이미지
            switch (post.getType()) {
                case BOAT -> imageUrl = "/images/boat.jpg";
                case FLOAT -> imageUrl = "/images/float.png";
                case ISLAND -> imageUrl = "/images/island.jpg";
                case ROCK -> imageUrl = "/images/rock.jpg";
                case STAY -> imageUrl = "/images/stay.png";
                default -> imageUrl = "/images/default.jpg";
            }
        } else if (!imageUrl.startsWith("/uploads/") && !imageUrl.startsWith("/images/")) {
            imageUrl = "/uploads/reservation_images/" + imageUrl;
        }

        // ✅ 한글 예약 타입명 가져오기
        ReservationType type = post.getType();
        String typeKorean = type != null ? type.getKorean() : "기타";

        // ✅ DTO 생성 및 반환
        return new ReservationCardDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCompanyName(),
                imageUrl,
                regionText,
                post.getFishTypes().stream()
                        .map(FishTypeEntity::getName)
                        .collect(Collectors.toList()),
                typeKorean
        );
    }

}
