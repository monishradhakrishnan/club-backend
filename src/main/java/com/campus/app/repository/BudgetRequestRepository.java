package com.campus.app.repository;

import com.campus.app.entity.BudgetRequest;
import com.campus.app.enums.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRequestRepository extends JpaRepository<BudgetRequest, Long> {
    List<BudgetRequest> findByClubId(Long clubId);
    List<BudgetRequest> findByRequestedById(Long studentId);
    List<BudgetRequest> findByStatus(BudgetStatus status);
    // Requests for clubs coordinated by a given staff member
    List<BudgetRequest> findByClub_Coordinator_Id(Long staffId);
}
