package com.mcc.api.service;

import com.mcc.api.model.Suggestion;
import com.mcc.api.model.User;
import com.mcc.api.repository.SuggestionRepository;
import com.mcc.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Suggestion create(String message, UUID userId) {
        Suggestion suggestion = new Suggestion();
        suggestion.setMessage(message);
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            suggestion.setUser(user);
        }
        return suggestionRepository.save(suggestion);
    }

    @Transactional(readOnly = true)
    public List<Suggestion> getAll() {
        return suggestionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void delete(UUID id) {
        suggestionRepository.deleteById(id);
    }
}
