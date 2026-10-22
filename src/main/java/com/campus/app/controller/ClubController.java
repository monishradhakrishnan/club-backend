package com.campus.app.controller;

import com.campus.app.dto.ClubRequest;
import com.campus.app.dto.ClubSummaryResponse;
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

    // Registered under literal paths ahead of /{id} on purpose — Spring's handler
    // mapping prefers exact literal matches over the {id} variable pattern, but
    // keep these declared here so it's obvious "joined"/"coordinating" are never
    // treated as a club id.

    // Student home page: clubs the logged-in student is registered in
    @GetMapping("/joined")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ClubSummaryResponse>> myJoinedClubs(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clubService.myJoinedClubs(principal.getId()));
    }

    // Staff home page: clubs the logged-in staff/admin user created
    @GetMapping("/coordinating")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<ClubSummaryResponse>> myCreatedClubs(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clubService.myCreatedClubs(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Club> getClub(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getClub(id));
    }

    // Route-level rule in SecurityConfig already restricts most /api/clubs/** writes to STAFF/ADMIN;
    // @PreAuthorize here documents intent at the method level too. Coordinator is
    // always the logged-in user — never accepted from the client.
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Club> createClub(@Valid @RequestBody ClubRequest req,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(clubService.createClub(req, principal.getId()));
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
