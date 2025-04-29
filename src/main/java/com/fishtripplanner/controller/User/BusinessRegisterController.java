package com.fishtripplanner.controller.User;
import com.fishtripplanner.domain.BusinessInfo;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.UserRole;
import com.fishtripplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class BusinessRegisterController {

    private final UserRepository userRepository;



    @PostMapping("/register/business")
    public String registerBusinessUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("company") String company,
            @RequestParam("address") String address,
            @RequestParam("address2") String address2,
            @RequestParam("nickname") String nickname,
            @RequestParam("service") String service,
            @RequestParam("phonenumber") Number phonenumber,
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
                .nickname(nickname)
                .name(name)
                .phonenumber(phonenumber.toString())
                .address(address + " " + address2)
                .birthyear(birthyear)
                .birthday(birthday)
                .age(age)
                .gender(gender)
                .role(UserRole.OWNER) // 예: BUSINESS 또는 OWNER 같은 enum 값
                .createdAt(now)
                .build();

        // 2. BusinessInfo 생성 및 연결
        BusinessInfo businessInfo = new BusinessInfo();
        businessInfo.setUser(user);
        businessInfo.setCompanyName(company);
        businessInfo.setAddress(address + " " + address2);
        businessInfo.setServiceTypes(Set.of(service)); // 다중 선택이면 Set.of(...)

        user.setBusinessInfo(businessInfo); // 양방향 연결

        // 3. 저장
        userRepository.save(user); // Cascade.ALL 덕분에 BusinessInfo도 함께 저장됨

        return "redirect:/login";
    }
}



