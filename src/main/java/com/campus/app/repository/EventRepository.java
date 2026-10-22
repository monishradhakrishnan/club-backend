package com.campus.app.repository;

import com.campus.app.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByClubIdOrderByEventDateAsc(Long clubId);
}
