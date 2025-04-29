package com.fishtripplanner.controller.join;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.dto.user.JoinRequest;
import com.fishtripplanner.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class JoinController {

    @GetMapping("/join")
    public String joinChoice() {
        return "joinChoice"; // 위 html 경로
    }

    @GetMapping("/join/normal")
    public String generalJoinForm() {
        return "joinNormal"; // 일반회원 폼
    }

    @GetMapping("/join/business")
    public String businessJoinForm() {
        return "joinBusiness"; // 사업자회원 폼
    }

    @GetMapping("/join/oauth")
    public String registerWithOAuth(HttpSession session, Model model) {
        model.addAttribute("nickname", session.getAttribute("oauth_nickname"));
        model.addAttribute("profileImage", session.getAttribute("oauth_profile_image"));
        model.addAttribute("email", session.getAttribute("oauth_email"));
        model.addAttribute("gender", session.getAttribute("oauth_gender"));
        model.addAttribute("age", session.getAttribute("oauth_age"));
        model.addAttribute("birthday", session.getAttribute("oauth_birthday"));
        model.addAttribute("birthyear", session.getAttribute("oauth_birthyear"));
        model.addAttribute("mobile", session.getAttribute("oauth_mobile"));
        model.addAttribute("name", session.getAttribute("oauth_name"));



        return "join/oauthJoinForm"; // 따로 페이지 만들거나 기존 회원가입 폼 재사용
    }
}