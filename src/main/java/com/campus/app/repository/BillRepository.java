package com.campus.app.repository;

import com.campus.app.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findAllByOrderByCreatedAtDesc();

    List<Bill> findByUploadedByIdOrderByCreatedAtDesc(Long userId);

    // Staff: bills they uploaded + bills for clubs they coordinate
    @Query("select b from Bill b left join b.club c " +
           "where b.uploadedBy.id = :userId or c.coordinator.id = :userId " +
           "order by b.createdAt desc")
    List<Bill> findVisibleToStaff(@Param("userId") Long userId);
}
