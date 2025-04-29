package com.fishtripplanner.controller.Party;

import com.fishtripplanner.domain.party.Party;
import com.fishtripplanner.domain.party.Waypoint;
import com.fishtripplanner.domain.party.WaypointType;
import com.fishtripplanner.dto.party.PartyCreateRequest;
import com.fishtripplanner.repository.PartyRepository;
import com.fishtripplanner.repository.WaypointRepository;
import com.fishtripplanner.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/party")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @GetMapping("/create")
    public String createPartyForm(Model model) {
        model.addAttribute("partyForm", new PartyCreateRequest());
        return "party/partyForm";
    }

    @PostMapping("/create")
    public String createParty(@ModelAttribute PartyCreateRequest partyCreateRequest, RedirectAttributes redirectAttributes) {
        Party savedParty = partyService.createParty(partyCreateRequest);  // 저장 후 Party 반환
        redirectAttributes.addAttribute("id", savedParty.getId());
        return "redirect:/party/{id}";  // 파티 상세 페이지로 이동
    }
}




