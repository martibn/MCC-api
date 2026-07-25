package com.mcc.api.controller;

import com.mcc.api.dto.response.LocationResponse;
import com.mcc.api.model.Location;
import com.mcc.api.model.User;
import com.mcc.api.repository.CardAcceptanceRepository;
import com.mcc.api.repository.LocationRepository;
import com.mcc.api.repository.UserRepository;
import com.mcc.api.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CardAcceptanceRepository cardAcceptanceRepository;
    private final LocationService locationService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        List<Location> userLocations = locationRepository.findByCreatedById(userId);
        List<LocationResponse> locationResponses = userLocations.stream()
                .map(locationService::toResponse)
                .toList();

        long acceptanceCount = cardAcceptanceRepository.countByReportedById(userId);

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("email", user.getEmail());
        body.put("name", user.getName());
        body.put("avatarUrl", user.getAvatarUrl());
        body.put("role", user.getRole().name());
        body.put("createdAt", user.getCreatedAt());
        body.put("locations", locationResponses);
        body.put("acceptanceCount", acceptanceCount);

        return ResponseEntity.ok(body);
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @RequestBody Map<String, String> body) {
        UUID userId = (UUID) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 8 characters"));
        }
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must contain at least one uppercase letter, one lowercase letter, and one digit"));
        }

        if (user.getPasswordHash() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot change password. Use Google login."));
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Current password is incorrect"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }
}
