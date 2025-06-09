package com.fishtripplanner.controller;

import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainPageController {

    private final PostRepository postRepository;

    // 루트 경로와 /MainPage 둘 다 같은 페이지로 연결
    @GetMapping({"/", "/MainPage"})
    public String showMainPage(Model model) {
        List<Post> posts = postRepository.findTop10ByOrderByCreatedAtDesc();
        List<Post> popularPosts = postRepository.findTop12ByOrderByViewCountDesc();

        model.addAttribute("posts", posts);
        model.addAttribute("popularPosts", popularPosts);

        return "MainPage"; // templates/MainPage.html 또는 templates/user/MainPage.html
    }
}
