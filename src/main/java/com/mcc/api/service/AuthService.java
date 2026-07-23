package com.mcc.api.service;

import com.mcc.api.dto.request.GoogleLoginRequest;
import com.mcc.api.dto.request.LoginRequest;
import com.mcc.api.dto.request.RefreshTokenRequest;
import com.mcc.api.dto.request.RegisterRequest;
import com.mcc.api.dto.response.AuthResponse;
import com.mcc.api.exception.BadRequestException;
import com.mcc.api.exception.UnauthorizedException;
import com.mcc.api.model.AuthProvider;
import com.mcc.api.model.User;
import com.mcc.api.repository.UserRepository;
import com.mcc.api.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setAuthProvider(AuthProvider.EMAIL);

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getAuthProvider() != AuthProvider.EMAIL) {
            throw new BadRequestException("This account uses Google login");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getGoogleToken());
            if (idToken == null) {
                throw new UnauthorizedException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String providerId = payload.getSubject();

            User user = userRepository.findByAuthProviderAndProviderId(AuthProvider.GOOGLE.name(), providerId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setAvatarUrl(pictureUrl);
                        newUser.setAuthProvider(AuthProvider.GOOGLE);
                        newUser.setProviderId(providerId);
                        return userRepository.save(newUser);
                    });

            return buildAuthResponse(user);
        } catch (Exception e) {
            throw new UnauthorizedException("Google authentication failed");
        }
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        var userId = jwtTokenProvider.getUserIdFromRefreshToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl(),
                jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail()),
                jwtTokenProvider.generateRefreshToken(user.getId())
        );
    }
}