package com.campus.app.service;

import com.campus.app.dto.BudgetRequestCreateDto;
import com.campus.app.dto.BudgetReviewDto;
import com.campus.app.entity.BudgetRequest;
import com.campus.app.entity.Club;
import com.campus.app.entity.User;
import com.campus.app.enums.BudgetStatus;
import com.campus.app.enums.Role;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.BudgetRequestRepository;
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetRequestService {

    private final BudgetRequestRepository budgetRequestRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetRequest createRequest(Long studentId, BudgetRequestCreateDto dto) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new ApiException("Club not found", HttpStatus.NOT_FOUND));

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

    public List<BudgetRequest> byClub(Long clubId) {
        return budgetRequestRepository.findByClubId(clubId);
    }

    public List<BudgetRequest> all() {
        return budgetRequestRepository.findAll();
    }
}
