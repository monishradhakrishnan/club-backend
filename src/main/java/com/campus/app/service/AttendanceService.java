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
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiry-seconds:300}")
    private int otpExpirySeconds;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Brute-force protection: max wrong OTPs per student inside the window.
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(10);
    private final Map<Long, Deque<Instant>> failedAttempts = new ConcurrentHashMap<>();

    /** Staff generates a fresh OTP for a club session (e.g. a meeting). */
    @Transactional
    public AttendanceSession generateOtp(Long staffId, GenerateOtpRequest req) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Club club = clubRepository.findById(req.getClubId())
                .orElseThrow(() -> new ApiException("Club not found", HttpStatus.NOT_FOUND));

        if (!club.getCoordinator().getId().equals(staffId) && staff.getRole().name().equals("STAFF")) {
            throw new ApiException("Only this club's coordinator can start an attendance session", HttpStatus.FORBIDDEN);
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceSession session = AttendanceSession.builder()
                .club(club)
                .createdBy(staff)
                .title(req.getTitle())
                .otp(generateUniqueOtp(now))
                .createdAt(now)
                .expiresAt(now.plusSeconds(otpExpirySeconds))
                .active(true)
                .build();

        return sessionRepository.save(session);
    }

    /**
     * Student submits ONLY the OTP. The session is found from the code itself,
     * restricted to active, unexpired sessions of clubs this student belongs to.
     */
    @Transactional
    public AttendanceRecord verifyAndMarkPresent(Long studentId, VerifyOtpRequest req) {
        assertNotLockedOut(studentId);

        LocalDateTime now = LocalDateTime.now();
        List<AttendanceSession> matches = sessionRepository
                .findActiveByOtpForStudent(req.getOtp(), studentId, now);

        if (matches.isEmpty()) {
            recordFailure(studentId);
            throw new ApiException(
                    "Invalid or expired OTP. Check the code with your coordinator.",
                    HttpStatus.BAD_REQUEST);
        }

        AttendanceSession session = matches.get(0);

        if (recordRepository.existsBySessionIdAndStudentId(session.getId(), studentId)) {
            throw new ApiException("Attendance already marked for this session", HttpStatus.CONFLICT);
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        AttendanceRecord record = AttendanceRecord.builder()
                .session(session)
                .student(student)
                .build();

        failedAttempts.remove(studentId);
        return recordRepository.save(record);
    }

    public List<AttendanceRecord> attendanceForSession(Long sessionId) {
        return recordRepository.findBySessionId(sessionId);
    }

    public List<AttendanceSession> sessionsForClub(Long clubId) {
        return sessionRepository.findByClubId(clubId);
    }

    public List<AttendanceRecord> myAttendanceForClub(Long studentId, Long clubId) {
        return recordRepository.findByStudentIdAndSession_ClubId(studentId, clubId);
    }

    @Transactional
    public void closeSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Session not found", HttpStatus.NOT_FOUND));
        session.setActive(false);
        sessionRepository.save(session);
    }

    // ---- helpers ----------------------------------------------------------

    private String generateUniqueOtp(LocalDateTime now) {
        for (int i = 0; i < 20; i++) {
            String code = generateOtpCode();
            if (!sessionRepository.existsByOtpAndActiveTrueAndExpiresAtAfter(code, now)) {
                return code;
            }
        }
        throw new ApiException("Could not generate a unique OTP, please try again",
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String generateOtpCode() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private void assertNotLockedOut(Long studentId) {
        Deque<Instant> attempts = failedAttempts.get(studentId);
        if (attempts == null) return;
        synchronized (attempts) {
            Instant cutoff = Instant.now().minus(LOCKOUT_WINDOW);
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
                attempts.pollFirst();
            }
            if (attempts.size() >= MAX_FAILED_ATTEMPTS) {
                throw new ApiException(
                        "Too many incorrect attempts. Please wait a few minutes and try again.",
                        HttpStatus.TOO_MANY_REQUESTS);
            }
        }
    }

    private void recordFailure(Long studentId) {
        Deque<Instant> attempts = failedAttempts.computeIfAbsent(studentId, k -> new ArrayDeque<>());
        synchronized (attempts) {
            attempts.addLast(Instant.now());
        }
    }
}