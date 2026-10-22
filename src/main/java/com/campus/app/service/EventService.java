package com.campus.app.service;

import com.campus.app.dto.EventRequest;
import com.campus.app.entity.Club;
import com.campus.app.entity.Event;
import com.campus.app.entity.User;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.ClubRepository;
import com.campus.app.repository.EventRepository;
import com.campus.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Transactional
    public Event createEvent(Long staffId, EventRequest req) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Club club = clubRepository.findById(req.getClubId())
                .orElseThrow(() -> new ApiException("Club not found", HttpStatus.NOT_FOUND));

        // Only this club's own coordinator (or an ADMIN) can create its events —
        // same ownership pattern as AttendanceService.generateOtp.
        if (!club.getCoordinator().getId().equals(staffId) && staff.getRole().name().equals("STAFF")) {
            throw new ApiException("Only this club's coordinator can create its events", HttpStatus.FORBIDDEN);
        }

        Event event = Event.builder()
                .club(club)
                .title(req.getTitle())
                .description(req.getDescription())
                .eventDate(req.getEventDate())
                .createdBy(staff)
                .build();

        return eventRepository.save(event);
    }

    public List<Event> listEventsForClub(Long clubId) {
        return eventRepository.findByClubIdOrderByEventDateAsc(clubId);
    }
}
