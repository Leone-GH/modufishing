package com.fishtripplanner.domain.party;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    private String title;

    @Column(length = 1000)
    private String description;

    private String region;

    private String departurePoint;
    private double departureLat;
    private double departureLng;

    private String destination;
    private double destinationLat;
    private double destinationLng;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private int estimatedCost;
    private int expectedDuration;
    private int maxParticipants;

    @Builder.Default
    private boolean closed = false;

    @ElementCollection
    private List<String> stopovers;

    @ElementCollection
    private List<Integer> stopoverStayTimes;

    @Enumerated(EnumType.STRING)
    private PartyStatus status;

    private LocalDateTime createdAt;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "party_participants",
            joinColumns = @JoinColumn(name = "party_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> participants = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "party_approved_participants",
            joinColumns = @JoinColumn(name = "party_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> approvedParticipants = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    private List<String> board = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    private List<String> chat = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "reservation_post_id")
    private ReservationPost reservationPost;


}
