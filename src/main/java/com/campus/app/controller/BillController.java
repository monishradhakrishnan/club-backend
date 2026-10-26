package com.campus.app.controller;

import com.campus.app.dto.BillCreateRequest;
import com.campus.app.dto.BillExtractionResponse;
import com.campus.app.dto.BillResponse;
import com.campus.app.security.UserPrincipal;
import com.campus.app.service.BillExcelService;
import com.campus.app.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Bill upload flow:
 *   1. POST /api/bills/extract  (multipart "file")  -> ML reads the PDF, returns a preview
 *   2. POST /api/bills          (JSON)              -> user-confirmed details are saved
 *   3. GET  /api/bills/export                       -> Excel sheet of all visible bills
 */
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final BillService billService;
    private final BillExcelService billExcelService;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BillExtractionResponse> extract(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(billService.extract(file));
    }

    @PostMapping
    public ResponseEntity<BillResponse> save(@Valid @RequestBody BillCreateRequest request,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billService.save(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<BillResponse>> list(@RequestParam(required = false) Long clubId,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billService.list(principal.getUser(), clubId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long clubId,
                                         @RequestParam(required = false) List<Long> ids,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        // ?ids=5 -> a fresh Excel with just that bill; no ids -> every bill you can see
        byte[] xlsx = billExcelService.build(billService.visibleBills(principal.getUser(), clubId, ids));
        String name = (ids != null && ids.size() == 1)
                ? "bill-" + ids.get(0) + "-" + LocalDate.now() + ".xlsx"
                : "club-bills-" + LocalDate.now() + ".xlsx";
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(name)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(XLSX)
                .body(xlsx);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        billService.delete(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
