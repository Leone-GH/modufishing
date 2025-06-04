package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
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
    private String region;  // 여러 지역 문자열로 표시
    private List<String> fishTypes;

    public static ReservationCardDto from(ReservationPost post) {
        String regionText = "미지정";

        List<RegionEntity> regions = post.getRegions();
        if (regions != null && !regions.isEmpty()) {
            // 부모 이름 -> 자식 목록 매핑
            Map<String, List<RegionEntity>> groupedByParent = new LinkedHashMap<>();

            for (RegionEntity region : regions) {
                String parentName;
                RegionEntity parent;

                if (region.getParent() != null) {
                    parent = region.getParent();
                    parentName = parent.getName();
                } else {
                    parent = region;
                    parentName = region.getName();
                }

                groupedByParent.computeIfAbsent(parentName, k -> new ArrayList<>()).add(region);
            }

            List<String> displayStrings = new ArrayList<>();

            for (Map.Entry<String, List<RegionEntity>> entry : groupedByParent.entrySet()) {
                String parentName = entry.getKey();
                List<RegionEntity> selected = entry.getValue();

                RegionEntity sampleRegion = selected.get(0).getParent() != null
                        ? selected.get(0).getParent()
                        : selected.get(0);

                List<RegionEntity> allChildren = sampleRegion.getChildren();
                Set<Long> selectedIds = selected.stream().map(RegionEntity::getId).collect(Collectors.toSet());

                boolean isAllChildrenSelected = allChildren != null && !allChildren.isEmpty() &&
                        allChildren.stream().map(RegionEntity::getId).allMatch(selectedIds::contains);

                if (isAllChildrenSelected) {
                    displayStrings.add("(" + parentName + ") 전체");
                } else {
                    displayStrings.addAll(
                            selected.stream()
                                    .filter(r -> r.getParent() != null) // 자식만
                                    .map(r -> "(" + parentName + ") " + r.getName())
                                    .toList()
                    );
                }
            }

            regionText = String.join(", ", displayStrings);
        }

        // 이미지 처리
        String imageUrl = post.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
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

        return new ReservationCardDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCompanyName(),
                imageUrl,
                regionText, // 🔁 지역 문자열 먼저
                post.getFishTypes().stream()
                        .map(FishTypeEntity::getName)
                        .collect(Collectors.toList())
        );
    }
}
