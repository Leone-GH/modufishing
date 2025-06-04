package com.fishtripplanner.controller;

import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.domain.reservation.BoatReservation;
import com.fishtripplanner.repository.BoatReservationRepository;
import com.fishtripplanner.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final PostRepository postRepository;
    private final BoatReservationRepository boatReservationRepository;

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {
        List<Post> results = postRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);

        List<BoatReservation> reservations = boatReservationRepository
                .findByCompanyNameContainingIgnoreCaseOrRegionContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrFishTypesContainingIgnoreCaseOrBoatTypeContainingIgnoreCase(
                        keyword, keyword, keyword, keyword, keyword);

        model.addAttribute("results", results);
        model.addAttribute("reservations", reservations);
        model.addAttribute("keyword", keyword);

        return "board/search_result";
    }
}
