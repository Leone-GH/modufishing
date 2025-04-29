package com.fishtripplanner.domain.party;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double lat;
    private double lng;
    private int stayTime; // 머무는 시간 (분)
    private int orderIndex; // 경유 순서 (0: 출발지, n: 경유지, 마지막: 목적지)

    @Enumerated(EnumType.STRING)
    private WaypointType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;
}
