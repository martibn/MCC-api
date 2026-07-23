package com.mcc.api.controller;

import com.mcc.api.dto.request.LocationRequest;
import com.mcc.api.dto.response.LocationResponse;
import com.mcc.api.model.ServiceCategory;
import com.mcc.api.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> search(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double minLng,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double maxLng,
            @RequestParam(required = false) ServiceCategory category,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) Boolean works,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(locationService.search(lat, lng, radius, minLat, minLng, maxLat, maxLng, category, cardType, works, search));
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody LocationRequest request,
                                                    Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody LocationRequest request,
                                                    Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(locationService.update(id, request, userId));
    }
}