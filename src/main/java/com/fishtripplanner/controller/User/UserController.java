package com.fishtripplanner.controller.User;

import com.fishtripplanner.api.user.UserService;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        User user = userService.findById(userId);
        model.addAttribute("user", user);
        return "user/notification";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        User user = userService.findById(userId);
        model.addAttribute("user", user);
        return "user/profile_edit";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        User savedUser = userService.updateUser(userId, updatedUser);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(savedUser),
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "redirect:/profile";
    }
}
