package com.campus.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    @NotNull
    private Long clubId;
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private LocalDateTime eventDate;
}
