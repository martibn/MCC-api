package com.mcc.api.repository;

import com.mcc.api.model.CardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardTypeRepository extends JpaRepository<CardType, UUID> {
    List<CardType> findByEnabledTrue();
    Optional<CardType> findByName(String name);
}
