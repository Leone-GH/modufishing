package com.fishtripplanner.controller.party;

import com.fishtripplanner.domain.party.Party;
import com.fishtripplanner.dto.party.PartyCreateRequest;
import com.fishtripplanner.repository.PartyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/party")
@RequiredArgsConstructor
public class PartyController {

    private final PartyRepository partyRepository;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("partyCreateRequest", new PartyCreateRequest());
        return "party/create"; // templates/party/create.html
    }

    @PostMapping("/saveRouteInfo")
    public String createParty(
            @ModelAttribute @Valid PartyCreateRequest req,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("error", "입력값 검증 실패");
            return "party/create";
        }
        if (!req.isValidPeriod()) {
            model.addAttribute("error", "마감일자는 출발일시보다 빨라야 합니다.");
            return "party/create";
        }

        Party party = Party.builder()
                .title(req.getTitle())
                .departurePoint(req.getDeparturePoint())
                .destination(req.getDestination())
                .waypoint(req.getWaypoint())
                .departureLat(req.getDepartureLat())
                .departureLng(req.getDepartureLng())
                .destinationLat(req.getDestinationLat())
                .destinationLng(req.getDestinationLng())
                .triptype(req.getTriptype())
                .departureDate(req.getDepartureDate())
                .deadlineDate(req.getDeadlineDate())
                .maxPerson(req.getMaxPerson())
                .carInfo(req.getCarInfo())
                .fuelCostEstimate(req.getFuelCostEstimate())
                .routePathJson(req.getRoutePathJson())
                .spec(req.getSpec())
                .userid(req.getUserid())
                .build();

        partyRepository.save(party);
        return "redirect:/party/list"; // 목록 페이지로 이동
    }
}

