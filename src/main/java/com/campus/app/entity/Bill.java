package com.campus.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A bill/invoice uploaded as a PDF. The PDF itself is not stored - only the
 * details extracted from it (and confirmed/corrected by the user).
 */
@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    // Optional: which club this expense belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    private String vendor;

    @Column(name = "bill_number", length = 100)
    private String billNumber;

    @Column(name = "bill_date")
    private LocalDate billDate;

    @Builder.Default
    @Column(length = 10)
    private String currency = "INR";

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 100)
    private String category;

    @Column(name = "file_name")
    private String fileName;

    // 0..1 score from the extractor, kept so reviewers can spot shaky reads
    private Double confidence;

    // Line items as JSON: [{"description":..,"quantity":..,"rate":..,"amount":..}]
    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
