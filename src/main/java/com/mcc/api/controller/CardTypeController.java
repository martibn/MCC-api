package com.mcc.api.controller;

import com.mcc.api.model.CardType;
import com.mcc.api.service.CardTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/card-types")
@RequiredArgsConstructor
public class CardTypeController {

    private final CardTypeService cardTypeService;

    @GetMapping
    public ResponseEntity<List<CardType>> getEnabled() {
        return ResponseEntity.ok(cardTypeService.getEnabled());
    }
}
