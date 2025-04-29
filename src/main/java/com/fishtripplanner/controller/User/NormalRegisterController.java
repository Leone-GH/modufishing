package com.fishtripplanner.controller.User;

import com.fishtripplanner.domain.BusinessInfo;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.UserRole;
import com.fishtripplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class NormalRegisterController {
        private final UserRepository userRepository;


        @PostMapping("/register/normal")
        public String registerBusinessUser(
                @RequestParam String username,
                @RequestParam String password,
                @RequestParam String email,
                @RequestParam String name,
                @RequestParam String nickname,
                @RequestParam String address,
                @RequestParam String address2,
                @RequestParam Number phonenumber,
                @RequestParam String birthyear,
                @RequestParam String birthday,
                @RequestParam String gender,
                @RequestParam String age

        ) {
            // 가입일시
            LocalDateTime now = LocalDateTime.now();

            // 1. User 생성
            User user = User.builder()
                    .username(username)
                    .password(password) // 비밀번호 암호화
                    .email(email)
                    .name(name)
                    .nickname(nickname)
                    .phonenumber(phonenumber.toString())
                    .address(address + " " + address2)
                    .role(UserRole.NORMAL) // 예: BUSINESS 또는 OWNER 같은 enum 값
                    .createdAt(now)
                    .birthyear(birthyear)
                    .birthday(birthday)
                    .age(age)
                    .gender(gender)
                    .build();

            // 2. 저장
            userRepository.save(user); // Cascade.ALL 덕분에 BusinessInfo도 함께 저장됨

            return "redirect:/login";
        }
}
