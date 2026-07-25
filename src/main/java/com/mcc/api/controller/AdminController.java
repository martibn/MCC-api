package com.mcc.api.controller;

import com.mcc.api.model.Suggestion;
import com.mcc.api.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SuggestionService suggestionService;

    @GetMapping("/suggestions")
    public ResponseEntity<Page<Suggestion>> getSuggestions(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(suggestionService.getAll(search, pageRequest));
    }

    @DeleteMapping("/suggestions/{id}")
    public ResponseEntity<?> deleteSuggestion(@PathVariable UUID id) {
        suggestionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
