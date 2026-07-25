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

    @PutMapping("/suggestions/{id}/status")
    public ResponseEntity<?> updateSuggestionStatus(@PathVariable UUID id,
                                                     @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || (!status.equals("PENDING") && !status.equals("READ") && !status.equals("RESOLVED"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status. Use PENDING, READ, or RESOLVED"));
        }
        Suggestion suggestion = suggestionService.updateStatus(id, status);
        return ResponseEntity.ok(Map.of("id", suggestion.getId(), "status", suggestion.getStatus()));
    }

    @DeleteMapping("/suggestions/{id}")
    public ResponseEntity<?> deleteSuggestion(@PathVariable UUID id) {
        suggestionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
