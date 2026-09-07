package com.campus.app.repository;

import com.campus.app.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByIdAndOtp(Long id, String otp);
    List<AttendanceSession> findByClubId(Long clubId);
    List<AttendanceSession> findByCreatedById(Long staffId);
}
