package com.campus.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotNull
    private Long sessionId;
    @NotBlank
    private String otp;
}
