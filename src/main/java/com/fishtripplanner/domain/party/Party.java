package com.fishtripplanner.domain.party;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;              // 모집글 제목
    private String departurePoint;     // 출발지 명칭
    private String destination;        // 도착지 명칭
    private String waypoint;           // 경유지(문자열/JSON, 복수 지원시 별도 엔티티 고려)
    private Double departureLat;
    private Double departureLng;
    private Double destinationLat;
    private Double destinationLng;

    private String triptype;           // 여행타입 (boat, rock)

    private LocalDateTime departureDate;   // 출발일시
    private LocalDateTime deadlineDate;    // 모집 마감일시

    private Integer maxPerson;         // 최대 인원수

    private String carInfo;            // 차량 모델명 등
    private Integer fuelCostEstimate;  // 예상 연료비(원)
    @Column(columnDefinition = "TEXT")
    private String routePathJson;      // 경로좌표(json문자열)

    @Column(length = 300)
    private String spec;               // 상세설명

    private String userid;             // 작성자 아이디

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
