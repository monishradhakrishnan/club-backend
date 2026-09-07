package com.campus.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Long coordinatorId; // staff user id
}
