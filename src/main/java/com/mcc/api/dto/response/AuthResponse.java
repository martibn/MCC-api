package com.mcc.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private UUID userId;
    private String email;
    private String name;
    private String avatarUrl;
    private String accessToken;
    private String refreshToken;
}
