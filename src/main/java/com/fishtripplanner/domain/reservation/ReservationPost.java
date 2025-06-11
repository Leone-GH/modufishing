package com.fishtripplanner.domain.reservation;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.entity.ReservationOrderEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;  // 예약 글을 작성한 사용자 (owner)

    @Enumerated(EnumType.STRING)
    private ReservationType type; // 예약 유형 (선상, 갯바위, 섬, 좌대, 숙박 등)

    private String title;  // 예약 글 제목

    @Column(columnDefinition = "TEXT")
    private String content;  // 예약 글 내용

    private int price;  // 예약 가격

    private String imageUrl;  // 예약 글에 첨부된 이미지 URL

    private LocalDateTime createdAt;  // 예약 글이 생성된 시간

    @Setter
    @Column(name = "company_name")
    private String companyName;  // 회사 이름 (예: 선박 회사, 숙소 이름 등)

    // 수정된 부분: 기존에는 하나의 지역만 설정할 수 있었지만, 이제는 여러 지역을 설정할 수 있도록 ManyToMany 관계로 수정
    @ManyToMany
    @JoinTable(
            name = "reservationpost_region",  // 지역과 예약글을 연결하는 중간 테이블
            joinColumns = @JoinColumn(name = "reservationpost_id"),  // 예약글 ID
            inverseJoinColumns = @JoinColumn(name = "region_id")  // 지역 ID
    )
    private List<RegionEntity> regions;  // 여러 지역을 하나의 예약글에 연결할 수 있도록 수정

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();  // 글이 생성될 때 생성 시간 자동 추가
    }

    @ManyToMany
    @BatchSize(size = 20)
    @JoinTable(
            name = "reservationpost_fishtype",  // 예약글과 어종을 연결하는 중간 테이블
            joinColumns = @JoinColumn(name = "reservationpost_id"),  // 예약글 ID
            inverseJoinColumns = @JoinColumn(name = "fish_type_id")  // 어종 ID
    )
    private List<FishTypeEntity> fishTypes;  // 예약글에 해당하는 어종 목록

    @OneToMany(mappedBy = "reservationPost")
    @BatchSize(size = 20)
    private List<ReservationPostAvailableDate> availableDates;  // 예약 글에 해당하는 예약 가능한 날짜들

    public String getFormattedRegionString() {
        if (regions == null || regions.isEmpty()) return "";

        // 부모 -> 자식 리스트 매핑
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (RegionEntity region : regions) {
            String parent = region.getParent() != null ? region.getParent().getName() : "기타";
            grouped.computeIfAbsent(parent, k -> new ArrayList<>()).add(region.getName());
        }

        return grouped.entrySet().stream()
                .map(e -> "(" + e.getKey() + ")" + String.join(" ", e.getValue()))
                .collect(Collectors.joining(", "));
    }

    @OneToMany(mappedBy = "reservationPost", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<ReservationOrderEntity> reservationOrders = new ArrayList<>();


    // 편의 메서드 추가: 예약글 작성 시 묶어서 초기화 가능
    public void setRegionAndContentAndPriceAndCompany(List<RegionEntity> regions, String content, int price, String companyName) {
        this.setRegions(regions);
        this.setContent(content);
        this.setPrice(price);
        this.setCompanyName(companyName);
    }

}
