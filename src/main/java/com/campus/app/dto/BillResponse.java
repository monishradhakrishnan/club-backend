package com.campus.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BillResponse(
        Long id,
        Long clubId,
        String clubName,
        String vendor,
        String billNumber,
        LocalDate billDate,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String category,
        String fileName,
        Double confidence,
        List<BillItemDto> items,
        String uploadedByName,
        String uploadedByRegisterNo,
        LocalDateTime createdAt
) {
}
