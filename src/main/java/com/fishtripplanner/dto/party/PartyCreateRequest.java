package com.fishtripplanner.dto.party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCreateRequest {
    private Long leaderId;
    private String title;
    private String description;
    private String region;
    private String departurePoint;
    private Double departureLat;
    private Double departureLng;
    private String destination;
    private Double destinationLat;
    private Double destinationLng;
    private LocalDateTime departureTime;
    private int maxParticipants;
    private int estimatedCost;
    private List<WaypointRequest> waypoints;
}
