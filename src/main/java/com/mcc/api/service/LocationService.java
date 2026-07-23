package com.mcc.api.service;

import com.mcc.api.dto.request.LocationRequest;
import com.mcc.api.dto.response.CardAcceptanceResponse;
import com.mcc.api.dto.response.LocationResponse;
import com.mcc.api.exception.ResourceNotFoundException;
import com.mcc.api.model.CardAcceptance;
import com.mcc.api.model.Location;
import com.mcc.api.model.ServiceCategory;
import com.mcc.api.model.User;
import com.mcc.api.repository.CardAcceptanceRepository;
import com.mcc.api.repository.LocationRepository;
import com.mcc.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final CardAcceptanceRepository cardAcceptanceRepository;
    private final UserRepository userRepository;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public LocationResponse create(LocationRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Location location = new Location();
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(request.getLng(), request.getLat())));
        location.setServiceCategory(request.getCategory());
        location.setCreatedBy(user);

        location = locationRepository.save(location);
        return toResponse(location);
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
        return toResponse(location);
    }

    @Transactional
    public LocationResponse update(UUID id, LocationRequest request, UUID userId) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));

        if (!location.getCreatedBy().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to update this location");
        }

        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(request.getLng(), request.getLat())));
        location.setServiceCategory(request.getCategory());

        location = locationRepository.save(location);
        return toResponse(location);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> search(Double lat, Double lng, Double radius,
                                          Double minLat, Double minLng, Double maxLat, Double maxLng,
                                          ServiceCategory category, String cardType, Boolean works,
                                          String search) {
        List<Location> locations;

        if (search != null && !search.isBlank()) {
            locations = locationRepository.searchByName(search);
        } else if (lat != null && lng != null && radius != null) {
            locations = locationRepository.findWithinRadius(lat, lng, radius);
        } else if (minLat != null && minLng != null && maxLat != null && maxLng != null) {
            locations = locationRepository.findWithinBounds(minLat, minLng, maxLat, maxLng);
        } else if (cardType != null && works != null) {
            locations = locationRepository.findByCardTypeAndWorks(cardType, works);
        } else if (cardType != null) {
            locations = locationRepository.findByCardType(cardType);
        } else if (works != null && works) {
            locations = locationRepository.findWithWorkingCards();
        } else if (works != null && !works) {
            locations = locationRepository.findWithNoWorkingCards();
        } else if (category != null) {
            locations = locationRepository.findByServiceCategory(category);
        } else {
            locations = locationRepository.findAll();
        }

        return locations.stream()
                .map(this::toResponse)
                .toList();
    }

    private LocationResponse toResponse(Location location) {
        List<CardAcceptance> acceptances = cardAcceptanceRepository.findByLocation(location);
        List<CardAcceptanceResponse> acceptanceResponses = acceptances.stream()
                .map(a -> new CardAcceptanceResponse(a.getId(), a.getCardType().name(), a.getWorks(), a.getCreatedAt()))
                .toList();

        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getLocation().getY(),
                location.getLocation().getX(),
                location.getServiceCategory(),
                location.getCreatedAt(),
                location.getUpdatedAt(),
                acceptanceResponses
        );
    }
}