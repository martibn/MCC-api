package com.mcc.api.repository;

import com.mcc.api.model.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {
    List<Suggestion> findAllByOrderByCreatedAtDesc();
}
