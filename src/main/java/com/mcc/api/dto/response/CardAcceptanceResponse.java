package com.mcc.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CardAcceptanceResponse {
    private UUID id;
    private String cardType;
    private Boolean works;
    private Instant createdAt;
}
