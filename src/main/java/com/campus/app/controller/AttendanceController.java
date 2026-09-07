package com.campus.app.controller;

import com.campus.app.dto.GenerateOtpRequest;
import com.campus.app.dto.VerifyOtpRequest;
import com.campus.app.entity.AttendanceRecord;
import com.campus.app.entity.AttendanceSession;
import com.campus.app.security.UserPrincipal;
import com.campus.app.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Staff generates an OTP for a club meeting/session
    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<AttendanceSession> generateOtp(@Valid @RequestBody GenerateOtpRequest req,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.generateOtp(principal.getId(), req));
    }

    @PatchMapping("/sessions/{id}/close")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Void> closeSession(@PathVariable Long id) {
        attendanceService.closeSession(id);
        return ResponseEntity.noContent().build();
    }

    // Student enters the OTP shown by staff to mark themselves present
    @PostMapping("/verify")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttendanceRecord> verify(@Valid @RequestBody VerifyOtpRequest req,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.verifyAndMarkPresent(principal.getId(), req));
    }

    @GetMapping("/sessions/{id}/records")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<AttendanceRecord>> records(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.attendanceForSession(id));
    }

    @GetMapping("/club/{clubId}/sessions")
    public ResponseEntity<List<AttendanceSession>> sessions(@PathVariable Long clubId) {
        return ResponseEntity.ok(attendanceService.sessionsForClub(clubId));
    }
}
