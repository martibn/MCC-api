package com.mcc.api.service;

import com.mcc.api.dto.request.CardAcceptanceRequest;
import com.mcc.api.dto.response.CardAcceptanceResponse;
import com.mcc.api.exception.ResourceNotFoundException;
import com.mcc.api.model.CardAcceptance;
import com.mcc.api.model.Location;
import com.mcc.api.model.User;
import com.mcc.api.repository.CardAcceptanceRepository;
import com.mcc.api.repository.LocationRepository;
import com.mcc.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardAcceptanceService {

    private final CardAcceptanceRepository cardAcceptanceRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CardAcceptanceResponse create(UUID locationId, CardAcceptanceRequest request, UUID userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CardAcceptance acceptance = new CardAcceptance();
        acceptance.setLocation(location);
        acceptance.setCardType(request.getCardType());
        acceptance.setWorks(request.getWorks());
        acceptance.setReportedBy(user);

        acceptance = cardAcceptanceRepository.save(acceptance);
        return toResponse(acceptance);
    }

    @Transactional(readOnly = true)
    public List<CardAcceptanceResponse> getByLocation(UUID locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location not found: " + locationId);
        }

        return cardAcceptanceRepository.findByLocationId(locationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CardAcceptanceResponse toResponse(CardAcceptance acceptance) {
        return new CardAcceptanceResponse(
                acceptance.getId(),
                acceptance.getCardType().name(),
                acceptance.getWorks(),
                acceptance.getCreatedAt()
        );
    }
}