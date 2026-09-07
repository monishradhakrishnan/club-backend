package com.campus.app.repository;

import com.campus.app.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    List<ClubMember> findByClubId(Long clubId);
    List<ClubMember> findByStudentId(Long studentId);
    Optional<ClubMember> findByClubIdAndStudentId(Long clubId, Long studentId);
    boolean existsByClubIdAndStudentId(Long clubId, Long studentId);
}
