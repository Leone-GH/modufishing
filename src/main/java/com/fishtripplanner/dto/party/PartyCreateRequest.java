// PartyCreateRequest.java
package com.fishtripplanner.dto.party;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class PartyCreateRequest {
    // 1차 폼 입력값
    private String triptype; // "boat" or "rock"
    private String departurePoint;
    private Double departureLat;
    private Double departureLng;
    private String destination;
    private Double destinationLat;
    private Double destinationLng;
    private List<WaypointRequest> waypoints;
    private LocalDateTime departureTime;
    private String marineInfoJson; // JS에서 hidden input에 저장

    // 2차 폼 입력값
    private String title;
    private String departureDesc;
    private String destinationDesc;
    private String waypointsDesc;
    private String description;      // 파티모집글 설명
    private LocalDateTime deadline;
    // 이미지 등은 Controller에서 따로 처리

    // 기타 필요한 필드는 알아서 추가
}

