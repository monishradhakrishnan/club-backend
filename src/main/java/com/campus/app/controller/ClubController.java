package com.campus.app.controller;

import com.campus.app.dto.ClubRequest;
import com.campus.app.entity.Club;
import com.campus.app.entity.ClubMember;
import com.campus.app.security.UserPrincipal;
import com.campus.app.service.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    // Listing clubs is useful for everyone (students browse to join)
    @GetMapping
    public ResponseEntity<List<Club>> listClubs() {
        return ResponseEntity.ok(clubService.listClubs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Club> getClub(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getClub(id));
    }

    // Route-level rule in SecurityConfig already restricts most /api/clubs/** writes to STAFF/ADMIN;
    // @PreAuthorize here documents intent at the method level too.
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Club> createClub(@Valid @RequestBody ClubRequest req) {
        return ResponseEntity.ok(clubService.createClub(req));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ClubMember>> members(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.listMembers(id));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClubMember> joinClub(@PathVariable Long id,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clubService.joinClub(id, principal.getId()));
    }
}
