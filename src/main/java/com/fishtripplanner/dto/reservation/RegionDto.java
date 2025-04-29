package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.entity.RegionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDto {
    private Long id;
    private String name;
    private String fullName;
    private List<RegionDto> children;

    public static RegionDto from(RegionEntity entity) {
        String fullName = entity.getParent() != null
                ? entity.getParent().getName() + " " + entity.getName()
                : entity.getName();

        List<RegionDto> children = entity.getChildren() == null ? List.of()
                : entity.getChildren().stream()
                .map(child -> {
                    String childFullName = entity.getName() + " " + child.getName();
                    return new RegionDto(child.getId(), child.getName(), childFullName, List.of());
                })
                .toList();

        return new RegionDto(entity.getId(), entity.getName(), fullName, children);
    }
}
