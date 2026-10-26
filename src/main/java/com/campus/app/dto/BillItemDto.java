package com.campus.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BillItemDto(String description, Double quantity, Double rate, Double amount) {
}
