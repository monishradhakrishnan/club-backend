package com.campus.app.service;

import com.campus.app.dto.GenerateOtpRequest;
import com.campus.app.dto.VerifyOtpRequest;
import com.campus.app.entity.AttendanceRecord;
import com.campus.app.entity.AttendanceSession;
import com.campus.app.entity.Club;
import com.campus.app.entity.User;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.AttendanceRecordRepository;
import com.campus.app.repository.AttendanceSessionRepository;
import com.campus.app.repository.ClubMemberRepository;
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiry-seconds:300}")
    private int otpExpirySeconds;

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Staff generates a fresh OTP for a club session (e.g. a meeting). */
    @Transactional
    public AttendanceSession generateOtp(Long staffId, GenerateOtpRequest req) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Club club = clubRepository.findById(req.getClubId())
                .orElseThrow(() -> new ApiException("Club not found", HttpStatus.NOT_FOUND));

        // Only the club's coordinator (or an ADMIN, enforced at controller level) generates OTPs
        if (!club.getCoordinator().getId().equals(staffId) && staff.getRole().name().equals("STAFF")) {
            throw new ApiException("Only this club's coordinator can start an attendance session", HttpStatus.FORBIDDEN);
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceSession session = AttendanceSession.builder()
                .club(club)
                .createdBy(staff)
                .title(req.getTitle())
                .otp(generateOtpCode())
                .createdAt(now)
                .expiresAt(now.plusSeconds(otpExpirySeconds))
                .active(true)
                .build();

        return sessionRepository.save(session);
    }

    /** Student submits the OTP shown by staff to mark themselves present. */
    @Transactional
    public AttendanceRecord verifyAndMarkPresent(Long studentId, VerifyOtpRequest req) {
        AttendanceSession session = sessionRepository.findById(req.getSessionId())
                .orElseThrow(() -> new ApiException("Attendance session not found", HttpStatus.NOT_FOUND));

        if (!session.isActive()) {
            throw new ApiException("This attendance session is closed", HttpStatus.GONE);
        }
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            session.setActive(false);
            sessionRepository.save(session);
            throw new ApiException("OTP has expired", HttpStatus.GONE);
        }
        if (!session.getOtp().equals(req.getOtp())) {
            throw new ApiException("Incorrect OTP", HttpStatus.BAD_REQUEST);
        }

        boolean isMember = clubMemberRepository.existsByClubIdAndStudentId(
                session.getClub().getId(), studentId);
        if (!isMember) {
            throw new ApiException("You must be a member of this club to mark attendance", HttpStatus.FORBIDDEN);
        }

        if (recordRepository.existsBySessionIdAndStudentId(session.getId(), studentId)) {
            throw new ApiException("Attendance already marked for this session", HttpStatus.CONFLICT);
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        AttendanceRecord record = AttendanceRecord.builder()
                .session(session)
                .student(student)
                .build();

        return recordRepository.save(record);
    }

    public List<AttendanceRecord> attendanceForSession(Long sessionId) {
        return recordRepository.findBySessionId(sessionId);
    }

    public List<AttendanceSession> sessionsForClub(Long clubId) {
        return sessionRepository.findByClubId(clubId);
    }

    @Transactional
    public void closeSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Session not found", HttpStatus.NOT_FOUND));
        session.setActive(false);
        sessionRepository.save(session);
    }

    private String generateOtpCode() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
