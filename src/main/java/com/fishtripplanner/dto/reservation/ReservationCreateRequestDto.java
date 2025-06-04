package com.fishtripplanner.dto.reservation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ReservationCreateRequestDto {
    private String title;
    private String type;
    private List<Long> regionIds;
    private List<String> fishTypeNames;
    private Integer price;
    private MultipartFile imageFile;
    private String content;
    private List<AvailableDateDto> availableDates;

    private Long userId; // ✅ 작성자 ID 추가

    private String companyName;  // ✅ 회사명 추가

    @Getter
    @Setter
    public static class AvailableDateDto {
        private String date;
        private String time;
        private Integer capacity;
    }
}