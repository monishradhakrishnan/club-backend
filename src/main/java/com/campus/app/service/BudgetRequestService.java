package com.campus.app.service;

import com.campus.app.dto.BudgetRequestCreateDto;
import com.campus.app.dto.BudgetRequestResponse;
import com.campus.app.dto.BudgetReviewDto;
import com.campus.app.entity.BudgetRequest;
import com.campus.app.entity.Club;
import com.campus.app.entity.User;
import com.campus.app.enums.BudgetStatus;
import com.campus.app.enums.Role;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.BudgetRequestRepository;
import com.campus.app.repository.ClubMemberRepository;
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetRequestService {

    private final BudgetRequestRepository budgetRequestRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubMemberRepository clubMemberRepository;

    @Transactional
    public BudgetRequest createRequest(Long studentId, BudgetRequestCreateDto dto) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new ApiException("Club not found", HttpStatus.NOT_FOUND));

        if (!clubMemberRepository.existsByClubIdAndStudentId(club.getId(), studentId)) {
            throw new ApiException("You can only send budget requests for clubs you have joined", HttpStatus.FORBIDDEN);
        }

        BudgetRequest request = BudgetRequest.builder()
                .club(club)
                .requestedBy(student)
                .title(dto.getTitle())
                .purpose(dto.getPurpose())
                .amount(dto.getAmount())
                .status(BudgetStatus.PENDING)
                .build();

        return budgetRequestRepository.save(request);
    }

    /**
     * Approve or reject a request. Only the coordinating staff member for the
     * request's club (or an ADMIN) may review it.
     */
    @Transactional
    public BudgetRequest review(Long requestId, Long reviewerId, BudgetReviewDto dto) {
        BudgetRequest request = budgetRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException("Budget request not found", HttpStatus.NOT_FOUND));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        boolean isClubCoordinator = request.getClub().getCoordinator().getId().equals(reviewerId);
        boolean isAdmin = reviewer.getRole() == Role.ADMIN;
        if (!isClubCoordinator && !isAdmin) {
            throw new ApiException("Only the club's staff coordinator can review this request", HttpStatus.FORBIDDEN);
        }

        if (request.getStatus() != BudgetStatus.PENDING) {
            throw new ApiException("This request has already been reviewed", HttpStatus.CONFLICT);
        }

        request.setStatus(dto.isApprove() ? BudgetStatus.APPROVED : BudgetStatus.REJECTED);
        request.setReviewedBy(reviewer);
        request.setReviewComment(dto.getComment());
        request.setReviewedAt(LocalDateTime.now());

        return budgetRequestRepository.save(request);
    }

    public List<BudgetRequest> myRequests(Long studentId) {
        return budgetRequestRepository.findByRequestedById(studentId);
    }

    /** Requests awaiting/relevant to a staff member across the clubs they coordinate. */
    public List<BudgetRequest> requestsForStaff(Long staffId) {
        return budgetRequestRepository.findByClub_Coordinator_Id(staffId);
    }

    /**
     * Same visibility as GET /mine (student: own, staff: clubs they coordinate, admin: all),
     * but as flat DTOs, newest first. Mapped inside the transaction so lazy fields load.
     */
    @Transactional(readOnly = true)
    public List<BudgetRequestResponse> inbox(User user) {
        List<BudgetRequest> requests = switch (user.getRole()) {
            case STUDENT -> budgetRequestRepository.findByRequestedById(user.getId());
            case STAFF -> budgetRequestRepository.findByClub_Coordinator_Id(user.getId());
            case ADMIN -> budgetRequestRepository.findAll();
        };
        return requests.stream()
                .sorted(Comparator.comparing(BudgetRequest::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private BudgetRequestResponse toResponse(BudgetRequest r) {
        return new BudgetRequestResponse(
                r.getId(),
                r.getClub().getId(),
                r.getClub().getName(),
                r.getTitle(),
                r.getPurpose(),
                r.getAmount(),
                r.getStatus().name(),
                r.getRequestedBy().getName(),
                r.getRequestedBy().getRegisterNo(),
                r.getReviewedBy() == null ? null : r.getReviewedBy().getName(),
                r.getReviewComment(),
                r.getCreatedAt(),
                r.getReviewedAt()
        );
    }

    public List<BudgetRequest> byClub(Long clubId) {
        return budgetRequestRepository.findByClubId(clubId);
    }

    public List<BudgetRequest> all() {
        return budgetRequestRepository.findAll();
    }
}
