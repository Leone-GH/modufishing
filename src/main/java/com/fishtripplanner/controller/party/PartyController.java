// PartyController.java
package com.fishtripplanner.controller.party;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.party.Party;
import com.fishtripplanner.domain.party.PartyMember;
import com.fishtripplanner.domain.party.Waypoint;
import com.fishtripplanner.domain.party.WaypointType;
import com.fishtripplanner.dto.party.PartyCreateRequest;
import com.fishtripplanner.dto.party.PartyMemberRequest;
import com.fishtripplanner.dto.party.WaypointRequest;
import com.fishtripplanner.repository.PartyMemberRepository;
import com.fishtripplanner.repository.PartyRepository;
import com.fishtripplanner.repository.UserRepository;
import com.fishtripplanner.repository.WaypointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

    private final PartyRepository partyRepository;
    private final WaypointRepository waypointRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserRepository userRepository;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("partyForm", new PartyCreateRequest());
        return "party/create";
    }

    @PostMapping("/save")
    @Transactional
    public String saveParty(@ModelAttribute PartyCreateRequest request) {
        Party party = Party.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .detail(request.getDetail())
                .region(request.getRegion())
                .departurePoint(request.getDeparturePoint())
                .departureLat(request.getDepartureLat())
                .departureLng(request.getDepartureLng())
                .destination(request.getDestination())
                .destinationLat(request.getDestinationLat())
                .destinationLng(request.getDestinationLng())
                .departureTime(request.getDepartureTime())
                .deadline(request.getDeadline())
                .maxParticipants(request.getMaxParticipants())
                .estimatedCost(request.getEstimatedCost())
                .memberDetail(request.getMemberDetail())
                .build();

        partyRepository.save(party);

        if (request.getWaypoints() != null) {
            int idx = 1;
            for (WaypointRequest w : request.getWaypoints()) {
                Waypoint waypoint = Waypoint.builder()
                        .party(party)
                        .name(w.getName())
                        .lat(w.getLat())
                        .lng(w.getLng())
                        .type(WaypointType.WAYPOINT)
                        .orderIndex(idx++)
                        .stayTime(w.getStayTime())
                        .build();
                waypointRepository.save(waypoint);
            }
        }

        if (request.getPartyMembers() != null) {
            for (PartyMemberRequest pmr : request.getPartyMembers()) {
                User user = userRepository.findByUsername(pmr.getUsername()).orElse(null);
                if (user != null) {
                    PartyMember pm = PartyMember.builder()
                            .party(party)
                            .user(user)
                            .joinedAt(pmr.getJoinedAt())
                            .build();
                    partyMemberRepository.save(pm);
                }
            }
        }

        return "redirect:/";
    }
}
