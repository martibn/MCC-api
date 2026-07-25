package com.mcc.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardAcceptanceRequest {
    @NotBlank
    private String cardType;
    @NotNull
    private Boolean works;
}
