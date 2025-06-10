package com.fishtripplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TideDto {
    private String recordTime; // 예: "12:00"
    private String tideCode;   // 예: "고조", "저조"
    private String tideLevel;  // 예: "120"

}

