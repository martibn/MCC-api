package com.mcc.api.dto.request;

import com.mcc.api.model.ServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @NotNull
    private Double lat;
    @NotNull
    private Double lng;
    @NotNull
    private ServiceCategory category;
}
