package com.campus.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubRequest {
    @NotBlank
    private String name;
    private String description;
    // Coordinator is always the logged-in staff/admin creating the club — never
    // taken from the client. No coordinatorId field here on purpose.
    @NotNull
    @Min(1)
    private Integer maxStudents;
}
