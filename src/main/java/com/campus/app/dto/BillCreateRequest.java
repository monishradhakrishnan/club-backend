package com.campus.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BillCreateRequest {

    private Long clubId; // optional

    @Size(max = 255)
    private String vendor;

    @Size(max = 100)
    private String billNumber;

    private LocalDate billDate;

    @Size(max = 10)
    private String currency;

    @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
    private BigDecimal subtotal;

    @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
    private BigDecimal taxAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalAmount;

    @Size(max = 100)
    private String category;

    @Size(max = 255)
    private String fileName;

    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double confidence;

    @Size(max = 100)
    private List<@Valid BillItemDto> items;
}
