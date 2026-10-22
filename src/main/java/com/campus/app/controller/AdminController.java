package com.campus.app.controller;

import com.campus.app.entity.User;
import com.campus.app.exception.ApiException;
import com.campus.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/pending-users")
    public List<User> pendingUsers() {
        return userRepository.findByEnabledFalse();
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User approved"));
    }

    @DeleteMapping("/users/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "User rejected and removed"));
    }
}