package com.mcc.api.repository;

import com.mcc.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByAuthProviderAndProviderId(String authProvider, String providerId);
    boolean existsByEmail(String email);
}