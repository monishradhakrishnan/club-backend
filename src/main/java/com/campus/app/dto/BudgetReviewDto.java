package com.campus.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BudgetReviewDto {
    @NotNull
    private boolean approve; // true = APPROVED, false = REJECTED
    private String comment;
}
