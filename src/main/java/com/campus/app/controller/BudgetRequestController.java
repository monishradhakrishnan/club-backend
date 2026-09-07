package com.campus.app.controller;

import com.campus.app.dto.BudgetRequestCreateDto;
import com.campus.app.dto.BudgetReviewDto;
import com.campus.app.entity.BudgetRequest;
import com.campus.app.enums.Role;
import com.campus.app.security.UserPrincipal;
import com.campus.app.service.BudgetRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget-requests")
@RequiredArgsConstructor
public class BudgetRequestController {

    private final BudgetRequestService budgetRequestService;

    // Student applies for budget
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BudgetRequest> create(@Valid @RequestBody BudgetRequestCreateDto dto,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(budgetRequestService.createRequest(principal.getId(), dto));
    }

    // Staff approves / rejects
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<BudgetRequest> review(@PathVariable Long id,
                                                 @Valid @RequestBody BudgetReviewDto dto,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(budgetRequestService.review(id, principal.getId(), dto));
    }

    // Student: my own requests. Staff/Admin: requests relevant to them.
    @GetMapping("/mine")
    public ResponseEntity<List<BudgetRequest>> mine(@AuthenticationPrincipal UserPrincipal principal) {
        var user = principal.getUser();
        List<BudgetRequest> result = switch (user.getRole()) {
            case STUDENT -> budgetRequestService.myRequests(user.getId());
            case STAFF -> budgetRequestService.requestsForStaff(user.getId());
            case ADMIN -> budgetRequestService.all();
        };
        return ResponseEntity.ok(result);
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<BudgetRequest>> byClub(@PathVariable Long clubId) {
        return ResponseEntity.ok(budgetRequestService.byClub(clubId));
    }
}
