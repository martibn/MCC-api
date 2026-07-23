package com.mcc.api.repository;

import com.mcc.api.model.CardAcceptance;
import com.mcc.api.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardAcceptanceRepository extends JpaRepository<CardAcceptance, UUID> {
    List<CardAcceptance> findByLocation(Location location);
    List<CardAcceptance> findByLocationId(UUID locationId);
    boolean existsByLocationIdAndCardTypeAndWorks(UUID locationId, String cardType, Boolean works);
}