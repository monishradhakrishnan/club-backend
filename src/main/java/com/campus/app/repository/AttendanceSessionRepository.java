package com.campus.app.repository;

import com.campus.app.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findByClubId(Long clubId);

    // Live session with this code, in a club the student belongs to
    @Query("""
        select s from AttendanceSession s
        where s.otp = :otp
          and s.active = true
          and s.expiresAt > :now
          and s.club.id in (select m.club.id from ClubMember m where m.student.id = :studentId)
        order by s.createdAt desc
        """)
    List<AttendanceSession> findActiveByOtpForStudent(@Param("otp") String otp,
                                                      @Param("studentId") Long studentId,
                                                      @Param("now") LocalDateTime now);

    // Used when generating, to avoid two live sessions sharing a code
    boolean existsByOtpAndActiveTrueAndExpiresAtAfter(String otp, LocalDateTime now);
}