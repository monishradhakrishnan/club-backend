package com.campus.app.service;

import com.campus.app.dto.ClubRequest;
import com.campus.app.entity.Club;
import com.campus.app.entity.ClubMember;
import com.campus.app.entity.User;
import com.campus.app.enums.Role;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.ClubMemberRepository;
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public Club createClub(ClubRequest req) {
        if (clubRepository.existsByName(req.getName())) {
            throw new ApiException("A club with this name already exists", HttpStatus.CONFLICT);
        }
        User coordinator = userRepository.findById(req.getCoordinatorId())
                .orElseThrow(() -> new ApiException("Coordinator not found", HttpStatus.NOT_FOUND));

        if (coordinator.getRole() != Role.STAFF && coordinator.getRole() != Role.ADMIN) {
            throw new ApiException("Coordinator must be a STAFF or ADMIN user", HttpStatus.BAD_REQUEST);
        }

        Club club = Club.builder()
                .name(req.getName())
                .description(req.getDescription())
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
}
