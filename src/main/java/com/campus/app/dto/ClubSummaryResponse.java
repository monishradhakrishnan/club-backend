package com.campus.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private Integer maxStudents;
    private long memberCount;
    private String coordinatorName;
    private String coordinatorRegisterNo;
}
