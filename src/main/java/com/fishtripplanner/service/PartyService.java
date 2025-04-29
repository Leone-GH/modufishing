package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.party.Party;
import com.fishtripplanner.domain.party.Waypoint;
import com.fishtripplanner.domain.party.WaypointType;
import com.fishtripplanner.dto.party.PartyCreateRequest;
import com.fishtripplanner.dto.party.WaypointRequest;
import com.fishtripplanner.repository.PartyRepository;
import com.fishtripplanner.repository.UserRepository;
import com.fishtripplanner.repository.WaypointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final WaypointRepository waypointRepository;

    public void createParty(PartyCreateRequest request) {
        User leader = userRepository.findById(request.getLeaderId()).orElseThrow();

        Party party = Party.builder()
                .leader(leader)
                .title(request.getTitle())
                .description(request.getDescription())
                .region(request.getRegion())
                .departurePoint(request.getDeparturePoint())
                .departureLat(request.getDepartureLat())
                .departureLng(request.getDepartureLng())
                .destination(request.getDestination())
                .destinationLat(request.getDestinationLat())
                .destinationLng(request.getDestinationLng())
                .departureTime(request.getDepartureTime())
                .maxParticipants(request.getMaxParticipants())
                .estimatedCost(request.getEstimatedCost())
                .createdAt(LocalDateTime.now())
                .build();

        partyRepository.save(party);

        List<Waypoint> waypoints = new ArrayList<>();

        if (request.getWaypoints() != null) {
            for (WaypointRequest wp : request.getWaypoints()) {
                Waypoint waypoint = Waypoint.builder()
                        .party(party)
                        .name(wp.getName())
                        .lat(wp.getLat())
                        .lng(wp.getLng())
                        .stayTime(wp.getStayTime())
                        .type(WaypointType.WAYPOINT)
                        .build();
                waypoints.add(waypoint);
            }
            waypointRepository.saveAll(waypoints);
        }
    }
}
