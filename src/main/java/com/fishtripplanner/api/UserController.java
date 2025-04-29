
package com.fishtripplanner.api;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.dto.user.JoinRequest;
import com.fishtripplanner.dto.user.LoginRequest;
import com.fishtripplanner.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        Optional<User> user = userRepository.findByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");
            return "login";
        }
        session.setAttribute("user", user);
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("joinRequest", new JoinRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute JoinRequest joinRequest) {
        User user = new User();
        user.setUsername(joinRequest.getUsername());
        user.setPassword(joinRequest.getPassword());
        user.setAddress(joinRequest.getAddress());
        user.setRole(joinRequest.getRole());
        userRepository.save(user);
        return "redirect:/login";
    }
}
