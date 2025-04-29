package com.fishtripplanner.security;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User user = token.getPrincipal();

        String registrationId = token.getAuthorizedClientRegistrationId();
        String name = null;
        String nickname = null;
        String profileImage = null;
        String email = null;
        String gender = null;
        String age = null;
        String birthday = null;
        String birthyear = null;
        String mobile = null;
        if (registrationId.equals("kakao")) {
            Map<String, Object> properties = (Map<String, Object>) user.getAttributes().get("properties");
            nickname = (String) properties.get("nickname");
            profileImage = (String) properties.get("profile_image");
            HttpSession session = request.getSession();


        } else if (registrationId.equals("naver")) {
            Map<String, Object> responseMap = (Map<String, Object>) user.getAttributes().get("response");
            nickname = (String) responseMap.get("nickname");
            profileImage = (String) responseMap.get("profile_image");
            email = (String) responseMap.get("email");
            gender = (String) responseMap.get("gender");
            age = (String) responseMap.get("age");
            birthday = (String) responseMap.get("birthday");
            birthyear = (String) responseMap.get("birthyear");
            mobile = (String) responseMap.get("mobile");
            name = (String) responseMap.get("name");
        }
        HttpSession session = request.getSession();
        session.setAttribute("oauth_nickname", nickname);
        session.setAttribute("oauth_profile_image", profileImage);
        session.setAttribute("oauth_email", email);
        session.setAttribute("oauth_gender", gender);
        session.setAttribute("oauth_age", age);
        session.setAttribute("oauth_birthday", birthday);
        session.setAttribute("oauth_birthyear", birthyear);
        session.setAttribute("oauth_mobile", mobile);
        session.setAttribute("oauth_name", name);





        Optional<User> existing = userRepository.findByNickname(nickname);
        if (existing.isPresent()) {
            // 로그인된 상태로 홈으로
            redirectStrategy.sendRedirect(request, response, "/");
        } else {
            // 회원가입 페이지로 이동
            redirectStrategy.sendRedirect(request, response, "/join/oauth");
        }
    }
}
