package com.mcc.api.controller;

import com.mcc.api.dto.request.CardAcceptanceRequest;
import com.mcc.api.dto.response.CardAcceptanceResponse;
import com.mcc.api.service.CardAcceptanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/locations/{locationId}/acceptances")
@RequiredArgsConstructor
public class CardAcceptanceController {

    private final CardAcceptanceService cardAcceptanceService;

    @PostMapping
    public ResponseEntity<CardAcceptanceResponse> create(@PathVariable UUID locationId,
                                                          @Valid @RequestBody CardAcceptanceRequest request,
                                                          Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardAcceptanceService.create(locationId, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<CardAcceptanceResponse>> getByLocation(@PathVariable UUID locationId) {
        return ResponseEntity.ok(cardAcceptanceService.getByLocation(locationId));
    }
}