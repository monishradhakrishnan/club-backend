package com.campus.app.service;

import com.campus.app.dto.AttendanceSummaryResponse;
import com.campus.app.dto.AttendanceSummaryResponse.SessionStatus;
import com.campus.app.entity.AttendanceSession;
import com.campus.app.repository.AttendanceRecordRepository;
import com.campus.app.repository.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceSummaryService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;

    /**
     * A student's attendance in one club: every session the club has held,
     * flagged attended / missed, plus totals. Deliberately does NOT expose
     * the session OTP.
     */
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse summaryForStudent(Long studentId, Long clubId) {
        List<AttendanceSession> sessions = sessionRepository.findByClubId(clubId);

        Set<Long> attendedIds = recordRepository
                .findByStudentIdAndSession_ClubId(studentId, clubId)
                .stream()
                .map(r -> r.getSession().getId())
                .collect(Collectors.toSet());

        List<SessionStatus> statuses = sessions.stream()
                .sorted(Comparator.comparing(AttendanceSession::getCreatedAt).reversed())
                .map(s -> new SessionStatus(
                        s.getId(), s.getTitle(), s.getCreatedAt(), attendedIds.contains(s.getId())))
                .toList();

        long attended = statuses.stream().filter(SessionStatus::attended).count();
        int percentage = statuses.isEmpty()
                ? 0
                : (int) Math.round(attended * 100.0 / statuses.size());

        return new AttendanceSummaryResponse(statuses.size(), attended, percentage, statuses);
    }
}