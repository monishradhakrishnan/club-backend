package com.campus.app.controller;

import com.campus.app.dto.AttendanceSummaryResponse;
import com.campus.app.dto.GenerateOtpRequest;
import com.campus.app.dto.VerifyOtpRequest;
import com.campus.app.entity.AttendanceRecord;
import com.campus.app.entity.AttendanceSession;
import com.campus.app.security.UserPrincipal;
import com.campus.app.service.AttendanceService;
import com.campus.app.service.AttendanceSummaryService;
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
    private final AttendanceSummaryService attendanceSummaryService;

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

    // Staff/admin only: this returns sessions including the live OTP,
    // so students must never be able to call it.
    @GetMapping("/club/{clubId}/sessions")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<AttendanceSession>> sessions(@PathVariable Long clubId) {
        return ResponseEntity.ok(attendanceService.sessionsForClub(clubId));
    }

    // Student's own attendance history for one club
    @GetMapping("/club/{clubId}/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AttendanceRecord>> myAttendanceForClub(@PathVariable Long clubId,
                                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.myAttendanceForClub(principal.getId(), clubId));
    }

    // Student's attended / total sessions for one club, with a per-session list
    @GetMapping("/club/{clubId}/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttendanceSummaryResponse> mySummary(@PathVariable Long clubId,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceSummaryService.summaryForStudent(principal.getId(), clubId));
    }
}