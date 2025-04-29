package com.fishtripplanner.controller.Party;

import com.fishtripplanner.dto.party.PartyCreateRequest;
import com.fishtripplanner.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

    private final PartyService partyService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("create", new PartyCreateRequest());
        return "party/create";
    }

    @PostMapping("/save")
    public String saveParty(@ModelAttribute PartyCreateRequest request) {
        partyService.createParty(request);
        return "redirect:/party/success";
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "party/success";  // templates/party/success.html
    }
}
