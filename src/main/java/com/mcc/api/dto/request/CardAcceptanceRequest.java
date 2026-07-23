package com.mcc.api.dto.request;

import com.mcc.api.model.CardType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardAcceptanceRequest {
    @NotNull
    private CardType cardType;
    @NotNull
    private Boolean works;
}
