package com.fishtripplanner.repository;

import com.fishtripplanner.domain.party.Waypoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaypointRepository extends JpaRepository<Waypoint, Long> {
    List<Waypoint> findByPartyId(Long partyId);
}
