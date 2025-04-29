package com.fishtripplanner.dto.party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaypointRequest {
    private String name;
    private double lat;
    private double lng;
    private int stayTime;
}
