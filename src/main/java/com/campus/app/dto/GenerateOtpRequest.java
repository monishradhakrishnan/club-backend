package com.campus.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateOtpRequest {
    @NotNull
    private Long clubId;
    @NotBlank
    private String title; // e.g. "Weekly Meeting - 12 Aug"
}
