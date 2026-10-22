package com.campus.app.service;

import com.campus.app.dto.ClubRequest;
import com.campus.app.dto.ClubSummaryResponse;
import com.campus.app.entity.Club;
import com.campus.app.entity.ClubMember;
import com.campus.app.entity.User;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.ClubMemberRepository;
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;

    // Coordinator is always the logged-in STAFF/ADMIN user creating the club —
    // never taken from the request body (route rule + @PreAuthorize already
    // restrict this method to STAFF/ADMIN, so no role re-check needed here).
    @Transactional
    public Club createClub(ClubRequest req, Long coordinatorId) {
        if (clubRepository.existsByName(req.getName())) {
            throw new ApiException("A club with this name already exists", HttpStatus.CONFLICT);
        }
        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new ApiException("Coordinator not found", HttpStatus.NOT_FOUND));

        Club club = Club.builder()
                .name(req.getName())
                .description(req.getDescription())
                .maxStudents(req.getMaxStudents())
                .coordinator(coordinator)
                .build();

        return clubRepository.save(club);
    }

    public List<Club> listClubs() {
        return clubRepository.findAll();
    }

    public Club getClub(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ApiException("Club not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ClubMember joinClub(Long clubId, Long studentId) {
        if (clubMemberRepository.existsByClubIdAndStudentId(clubId, studentId)) {
            throw new ApiException("Already a member of this club", HttpStatus.CONFLICT);
        }
        Club club = getClub(clubId);

        long currentMembers = clubMemberRepository.countByClubId(clubId);
        if (currentMembers >= club.getMaxStudents()) {
            throw new ApiException("This club has reached its registration limit", HttpStatus.CONFLICT);
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("Student not found", HttpStatus.NOT_FOUND));

        ClubMember member = ClubMember.builder()
                .club(club)
                .student(student)
                .build();

        return clubMemberRepository.save(member);
    }

    public List<ClubMember> listMembers(Long clubId) {
        return clubMemberRepository.findByClubId(clubId);
    }

    // Clubs a student is registered in — for the student home page boxes
    public List<ClubSummaryResponse> myJoinedClubs(Long studentId) {
        return clubMemberRepository.findByStudentId(studentId).stream()
                .map(ClubMember::getClub)
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    // Clubs a staff/admin user created — for the staff home page boxes
    public List<ClubSummaryResponse> myCreatedClubs(Long coordinatorId) {
        return clubRepository.findByCoordinatorId(coordinatorId).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private ClubSummaryResponse toSummary(Club club) {
        return ClubSummaryResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .maxStudents(club.getMaxStudents())
                .memberCount(clubMemberRepository.countByClubId(club.getId()))
                .coordinatorName(club.getCoordinator().getName())
                .coordinatorRegisterNo(club.getCoordinator().getRegisterNo())
                .build();
    }
}
