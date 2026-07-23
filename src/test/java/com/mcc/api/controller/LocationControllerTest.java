package com.mcc.api.controller;

import com.mcc.api.dto.response.LocationResponse;
import com.mcc.api.exception.ResourceNotFoundException;
import com.mcc.api.exception.GlobalExceptionHandler;
import com.mcc.api.model.ServiceCategory;
import com.mcc.api.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LocationControllerTest {

    private MockMvc mockMvc;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = mock(LocationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LocationController(locationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void search_noParams_returnsAll() throws Exception {
        LocationResponse loc = new LocationResponse(UUID.randomUUID(), "Bar Paco", "Carrer Major 1", 41.38, 2.17, ServiceCategory.BAR, Instant.now(), Instant.now(), List.of());
        when(locationService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(loc));

        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bar Paco"));
    }

    @Test
    void search_byCategory() throws Exception {
        LocationResponse loc = new LocationResponse(UUID.randomUUID(), "Cafe Moka", "Av. Diagonal 100", 41.39, 2.16, ServiceCategory.CAFETERIA, Instant.now(), Instant.now(), List.of());
        when(locationService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(ServiceCategory.CAFETERIA), isNull(), isNull(), isNull()))
                .thenReturn(List.of(loc));

        mockMvc.perform(get("/locations").param("category", "CAFETERIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceCategory").value("CAFETERIA"));
    }

    @Test
    void search_bySearchTerm() throws Exception {
        LocationResponse loc = new LocationResponse(UUID.randomUUID(), "Bar Paco", "Carrer Major 1", 41.38, 2.17, ServiceCategory.BAR, Instant.now(), Instant.now(), List.of());
        when(locationService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq("paco")))
                .thenReturn(List.of(loc));

        mockMvc.perform(get("/locations").param("search", "paco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bar Paco"));
    }

    @Test
    void search_emptyResults() throws Exception {
        when(locationService.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_found() throws Exception {
        UUID id = UUID.randomUUID();
        LocationResponse loc = new LocationResponse(id, "Bar Paco", "Carrer Major 1", 41.38, 2.17, ServiceCategory.BAR, Instant.now(), Instant.now(), List.of());
        when(locationService.getById(id)).thenReturn(loc);

        mockMvc.perform(get("/locations/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bar Paco"));
    }

    @Test
    void getById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(locationService.getById(id)).thenThrow(new ResourceNotFoundException("Location not found: " + id));

        mockMvc.perform(get("/locations/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found: " + id));
    }
}
