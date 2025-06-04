package com.fishtripplanner.entity;

import com.fishtripplanner.domain.reservation.ReservationPost;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "region")
public class RegionEntity {

    @Id
    @GeneratedValue
    private Long id;  // 지역 고유 ID

    private String name;  // 지역 이름

    @ManyToOne
    @JoinColumn(name = "parent_id")  // 상위 지역의 ID
    private RegionEntity parent;  // 상위 지역 (부모 지역)

    @OneToMany(mappedBy = "parent")
    private List<RegionEntity> children = new ArrayList<>();  // 자식 지역 목록

    // 수정된 부분: ReservationPost와의 ManyToMany 관계 추가
    @ManyToMany(mappedBy = "regions")  // 예약글에서 regions 필드에 의해 관리됨
    private List<ReservationPost> reservationPosts = new ArrayList<>();  // 이 지역을 포함한 예약 글 목록

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegionEntity)) return false;
        RegionEntity other = (RegionEntity) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);  // ID를 기준으로 hashCode 생성
    }
}
