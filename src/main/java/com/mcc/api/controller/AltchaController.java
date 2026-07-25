package com.mcc.api.controller;

import com.mcc.api.service.AltchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/altcha")
@RequiredArgsConstructor
public class AltchaController {

    private final AltchaService altchaService;

    @GetMapping("/challenge")
    public ResponseEntity<Map<String, Object>> getChallenge() {
        return ResponseEntity.ok(altchaService.generateChallenge());
    }
}
