package com.mcc.api.controller;

import com.mcc.api.dto.response.CardAcceptanceResponse;
import com.mcc.api.exception.ResourceNotFoundException;
import com.mcc.api.exception.GlobalExceptionHandler;
import com.mcc.api.model.CardType;
import com.mcc.api.service.CardAcceptanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardAcceptanceControllerTest {

    private MockMvc mockMvc;
    private CardAcceptanceService cardAcceptanceService;

    @BeforeEach
    void setUp() {
        cardAcceptanceService = mock(CardAcceptanceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CardAcceptanceController(cardAcceptanceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getByLocation_found() throws Exception {
        UUID locId = UUID.randomUUID();
        UUID acceptanceId = UUID.randomUUID();
        CardAcceptanceResponse resp = new CardAcceptanceResponse(acceptanceId, CardType.FLEXOH.name(), true, Instant.now());
        when(cardAcceptanceService.getByLocation(locId)).thenReturn(List.of(resp));

        mockMvc.perform(get("/locations/" + locId + "/acceptances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardType").value("FLEXOH"))
                .andExpect(jsonPath("$[0].works").value(true));
    }

    @Test
    void getByLocation_empty() throws Exception {
        UUID locId = UUID.randomUUID();
        when(cardAcceptanceService.getByLocation(locId)).thenReturn(List.of());

        mockMvc.perform(get("/locations/" + locId + "/acceptances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByLocation_locationNotFound() throws Exception {
        UUID locId = UUID.randomUUID();
        when(cardAcceptanceService.getByLocation(locId))
                .thenThrow(new ResourceNotFoundException("Location not found: " + locId));

        mockMvc.perform(get("/locations/" + locId + "/acceptances"))
                .andExpect(status().isNotFound());
    }
}
