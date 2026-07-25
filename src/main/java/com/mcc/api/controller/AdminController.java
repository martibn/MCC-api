package com.mcc.api.controller;

import com.mcc.api.model.Suggestion;
import com.mcc.api.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SuggestionService suggestionService;

    @GetMapping("/suggestions")
    public ResponseEntity<List<Suggestion>> getSuggestions() {
        return ResponseEntity.ok(suggestionService.getAll());
    }

    @DeleteMapping("/suggestions/{id}")
    public ResponseEntity<?> deleteSuggestion(@PathVariable UUID id) {
        suggestionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
