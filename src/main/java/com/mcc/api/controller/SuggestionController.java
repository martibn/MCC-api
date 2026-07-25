package com.mcc.api.controller;

import com.mcc.api.model.Suggestion;
import com.mcc.api.service.AltchaService;
import com.mcc.api.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;
    private final AltchaService altchaService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body,
                                     Authentication authentication) {
        String message = body.get("message");
        String altchaPayload = body.get("altchaPayload");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }
        if (altchaPayload == null || altchaPayload.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CAPTCHA is required"));
        }

        UUID userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            userId = (UUID) authentication.getPrincipal();
        }

        try {
            String algorithm = extractField(altchaPayload, "algorithm");
            String challenge = extractField(altchaPayload, "challenge");
            String salt = extractField(altchaPayload, "salt");
            int number = Integer.parseInt(extractField(altchaPayload, "number"));
            String signature = extractField(altchaPayload, "signature");
            if (!altchaService.verifyPayload(algorithm, challenge, salt, number, signature)) {
                return ResponseEntity.badRequest().body(Map.of("error", "CAPTCHA verification failed"));
            }

            Suggestion suggestion = suggestionService.create(message, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", suggestion.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) {
            search = "\"" + key + "\":";
            start = json.indexOf(search);
            if (start < 0) throw new IllegalArgumentException("Missing field: " + key);
            start += search.length();
            int end = json.indexOf(",", start);
            if (end < 0) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
