package com.campus.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * What the ML service (POST /extract-bill) returns. Also sent as-is to the app
 * as the "preview" the user confirms before saving, so field names stay snake_case.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillExtractionResponse {

    private String vendor;

    @JsonProperty("bill_number")
    private String billNumber;

    @JsonProperty("bill_date")
    private String billDate; // ISO yyyy-MM-dd

    private String currency;
    private Double subtotal;
    private Double tax;
    private Double total;
    private String category;
    private Double confidence;
    private List<BillItemDto> items;
    private List<String> warnings;

    @JsonProperty("used_ocr")
    private Boolean usedOcr;
}
