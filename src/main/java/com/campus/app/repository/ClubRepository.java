package com.campus.app.repository;

import com.campus.app.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    boolean existsByName(String name);
    List<Club> findByCoordinatorId(Long coordinatorId);
}
