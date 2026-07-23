package com.mcc.api.dto.response;

import com.mcc.api.model.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LocationResponse {
    private UUID id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private ServiceCategory serviceCategory;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CardAcceptanceResponse> acceptances;
}
