package com.mcc.api.repository;

import com.mcc.api.model.Suggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {
    Page<Suggestion> findByMessageContainingIgnoreCase(String search, Pageable pageable);
}
