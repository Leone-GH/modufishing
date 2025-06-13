package com.fishtripplanner.dto.party;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PartyCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String departurePoint;
    @NotBlank
    private String destination;
    private String waypoint;

    @NotNull
    private Double departureLat;
    @NotNull
    private Double departureLng;
    @NotNull
    private Double destinationLat;
    @NotNull
    private Double destinationLng;

    @NotBlank
    private String triptype; // boat, rock

    @NotNull
    private LocalDateTime departureDate;
    @NotNull
    private LocalDateTime deadlineDate;

    @Min(2)
    @Max(30)
    @NotNull
    private Integer maxPerson;

    private String carInfo;
    private Integer fuelCostEstimate;
    private String routePathJson;
    private String spec;

    @NotBlank
    private String userid;

    public boolean isValidPeriod() {
        if (deadlineDate == null || departureDate == null) return false;
        // 마감일이 출발일시 "이전"이어야 정상 등록
        return deadlineDate.isBefore(departureDate);
    }
}

