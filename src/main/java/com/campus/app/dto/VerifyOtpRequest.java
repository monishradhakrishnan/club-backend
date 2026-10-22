package com.campus.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{4,10}", message = "OTP must be digits only")
    private String otp;
}