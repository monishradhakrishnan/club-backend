package com.campus.app.service;

import com.campus.app.dto.*;
import com.campus.app.entity.Bill;
import com.campus.app.entity.Club;
import com.campus.app.entity.User;
import com.campus.app.enums.Role;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.BillRepository;
import com.campus.app.repository.ClubMemberRepository;
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BillService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;

    private final BillRepository billRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;
    private final MLService mlService;
    private final ObjectMapper objectMapper;

    /** Step 1: read the PDF with the ML service. Nothing is saved yet. */
    public BillExtractionResponse extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Please choose a PDF file", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApiException("PDF is larger than 10 MB", HttpStatus.PAYLOAD_TOO_LARGE);
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".pdf")) {
            throw new ApiException("Only PDF files are supported", HttpStatus.BAD_REQUEST);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ApiException("Could not read the uploaded file", HttpStatus.BAD_REQUEST);
        }
        if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F') {
            throw new ApiException("File is not a valid PDF", HttpStatus.BAD_REQUEST);
        }
        return mlService.extractBill(bytes);
    }

    /** Step 2: save the (user-confirmed) details. */
    @Transactional
    public BillResponse save(Long userId, BillCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Club club = null;
        if (req.getClubId() != null) {
            club = clubRepository.findById(req.getClubId())
                    .orElseThrow(() -> new ApiException("Club not found", HttpStatus.NOT_FOUND));
            assertCanUseClub(user, club);
        }

        Bill bill = Bill.builder()
                .uploadedBy(user)
                .club(club)
                .vendor(blankToNull(req.getVendor()))
                .billNumber(blankToNull(req.getBillNumber()))
                .billDate(req.getBillDate())
                .currency(blankToNull(req.getCurrency()) == null ? "INR" : req.getCurrency().trim())
                .subtotal(req.getSubtotal())
                .taxAmount(req.getTaxAmount())
                .totalAmount(req.getTotalAmount())
                .category(blankToNull(req.getCategory()))
                .fileName(blankToNull(req.getFileName()))
                .confidence(req.getConfidence())
                .itemsJson(writeItems(req.getItems()))
                .build();

        return toResponse(billRepository.save(bill));
    }

    @Transactional(readOnly = true)
    public List<BillResponse> list(User user, Long clubId) {
        return visibleBills(user, clubId).stream().map(this::toResponse).toList();
    }

    /** Raw entities for the Excel export (same visibility rules as list). */
    @Transactional(readOnly = true)
    public List<Bill> visibleBills(User user, Long clubId) {
        return visibleBills(user, clubId, null);
    }

    /** ids != null -> only those bills (still limited to what the user may see). */
    @Transactional(readOnly = true)
    public List<Bill> visibleBills(User user, Long clubId, List<Long> ids) {
        List<Bill> bills = switch (user.getRole()) {
            case ADMIN -> billRepository.findAllByOrderByCreatedAtDesc();
            case STAFF -> billRepository.findVisibleToStaff(user.getId());
            case STUDENT -> billRepository.findByUploadedByIdOrderByCreatedAtDesc(user.getId());
        };
        if (clubId != null) {
            bills = bills.stream()
                    .filter(b -> b.getClub() != null && clubId.equals(b.getClub().getId()))
                    .toList();
        }
        if (ids != null && !ids.isEmpty()) {
            bills = bills.stream().filter(b -> ids.contains(b.getId())).toList();
        }
        return bills;
    }

    @Transactional
    public void delete(User user, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ApiException("Bill not found", HttpStatus.NOT_FOUND));
        boolean owner = bill.getUploadedBy().getId().equals(user.getId());
        if (!owner && user.getRole() != Role.ADMIN) {
            throw new ApiException("You can only delete bills you uploaded", HttpStatus.FORBIDDEN);
        }
        billRepository.delete(bill);
    }

    // ---- helpers -------------------------------------------------------

    private void assertCanUseClub(User user, Club club) {
        switch (user.getRole()) {
            case ADMIN -> { /* allowed */ }
            case STAFF -> {
                if (!club.getCoordinator().getId().equals(user.getId())) {
                    throw new ApiException("You can only attach bills to clubs you coordinate", HttpStatus.FORBIDDEN);
                }
            }
            case STUDENT -> {
                if (!clubMemberRepository.existsByClubIdAndStudentId(club.getId(), user.getId())) {
                    throw new ApiException("You can only attach bills to clubs you have joined", HttpStatus.FORBIDDEN);
                }
            }
        }
    }

    public List<BillItemDto> readItems(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<BillItemDto>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String writeItems(List<BillItemDto> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new ApiException("Could not store bill line items", HttpStatus.BAD_REQUEST);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private BillResponse toResponse(Bill b) {
        Club club = b.getClub();
        User u = b.getUploadedBy();
        return new BillResponse(
                b.getId(),
                club == null ? null : club.getId(),
                club == null ? null : club.getName(),
                b.getVendor(),
                b.getBillNumber(),
                b.getBillDate(),
                b.getCurrency(),
                b.getSubtotal(),
                b.getTaxAmount(),
                b.getTotalAmount(),
                b.getCategory(),
                b.getFileName(),
                b.getConfidence(),
                readItems(b.getItemsJson()),
                u.getName(),
                u.getRegisterNo(),
                b.getCreatedAt()
        );
    }
}
