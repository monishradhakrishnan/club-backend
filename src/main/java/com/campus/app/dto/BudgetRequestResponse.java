package com.campus.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Flat view of a budget request for the app (avoids lazy-loaded entity fields serializing as null). */
public record BudgetRequestResponse(
        Long id,
        Long clubId,
        String clubName,
        String title,          // event name
        String purpose,
        BigDecimal amount,
        String status,         // PENDING | APPROVED | REJECTED
        String requestedByName,
        String requestedByRegisterNo,
        String reviewedByName,
        String reviewComment,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
}
