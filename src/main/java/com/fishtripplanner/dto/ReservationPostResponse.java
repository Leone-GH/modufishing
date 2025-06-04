package com.fishtripplanner.dto;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReservationPostResponse {
    private Long id;
    private String title;
    private String content;
    private String region;  // 여러 지역을 처리할 수 있도록 수정
    private String type;
    private List<LocalDate> availableDates;
    private int price;
    private String imageUrl;
    private LocalDateTime createdAt;

    // ✅ 정적 팩토리 메서드 사용
    public static ReservationPostResponse from(ReservationPost post) {
        // 여러 지역을 처리하는 코드로 수정
        String regionText = post.getRegions().stream()
                .map(region -> {
                    String parent = region.getParent() != null ? "(" + region.getParent().getName() + ") " : "";
                    return parent + region.getName();  // 지역 이름에 부모 지역 이름을 추가
                })
                .collect(Collectors.joining(", "));  // 지역들을 콤마로 구분하여 연결

        // 이미지 URL 설정
        String imageUrl = post.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = switch (post.getType()) {
                case BOAT -> "/images/boat.png";
                case STAY -> "/images/stay.png";
                case ISLAND -> "/images/island.png";
                case FLOAT -> "/images/float.png";
                case ROCK -> "/images/rock.png";
                default -> "/images/default.jpg";
            };
        }

        // 예약글 응답 객체 생성
        return ReservationPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .region(regionText)  // 여러 지역을 처리한 텍스트
                .type(post.getType().name())
                .price(post.getPrice())
                .imageUrl(imageUrl)
                .createdAt(post.getCreatedAt())
                .availableDates(
                        post.getAvailableDates().stream()
                                .map(ReservationPostAvailableDate::getAvailableDate)
                                .collect(Collectors.toList())  // 날짜 리스트로 변환
                )
                .build();
    }
}
