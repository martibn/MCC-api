package com.mcc.api.dto.request;

import com.mcc.api.model.ServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    @NotEmpty
    private List<ServiceCategory> categories;
}
