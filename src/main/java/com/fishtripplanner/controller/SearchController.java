package com.fishtripplanner.controller;

import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.repository.PostRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final PostRepository postRepository;
    private final ReservationPostRepository reservationPostRepository;

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {
        List<Post> results = postRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);

        List<ReservationPost> reservations = reservationPostRepository.findByKeyword(keyword);

        // 🔁 여기서 DTO로 변환
        List<ReservationCardDto> reservationDtos = reservations.stream()
                .map(ReservationCardDto::from)
                .toList();

        model.addAttribute("results", results);
        model.addAttribute("reservations", reservationDtos);  // 🔁 수정
        model.addAttribute("keyword", keyword);

        return "board/search_result";
    }

}
