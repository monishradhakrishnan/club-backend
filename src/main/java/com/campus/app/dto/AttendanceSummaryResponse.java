package com.campus.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AttendanceSummaryResponse(
        long totalSessions,
        long attendedSessions,
        int percentage,
        List<SessionStatus> sessions) {

    public record SessionStatus(Long id, String title, LocalDateTime date, boolean attended) {}
}