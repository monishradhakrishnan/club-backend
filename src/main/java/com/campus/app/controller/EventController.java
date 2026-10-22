package com.campus.app.controller;

import com.campus.app.dto.EventRequest;
import com.campus.app.entity.Event;
import com.campus.app.security.UserPrincipal;
import com.campus.app.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Staff creates an event for a club they coordinate (route-level rule in
    // SecurityConfig restricts POST to STAFF/ADMIN; @PreAuthorize documents intent
    // at the method level too; ownership of the specific club is checked in the service).
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest req,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(eventService.createEvent(principal.getId(), req));
    }

    // Any authenticated user (students included) can view a club's events
    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<Event>> eventsForClub(@PathVariable Long clubId) {
        return ResponseEntity.ok(eventService.listEventsForClub(clubId));
    }
}
