package com.marketplace.identity.service;

import com.marketplace.identity.dto.LoginRequest;
import com.marketplace.identity.dto.LoginResponse;
import com.marketplace.identity.dto.RegisterRequest;
import com.marketplace.identity.dto.RegisterResponse;
import com.marketplace.identity.exception.InvalidCredentialsException;
import com.marketplace.identity.exception.UserAlreadyExistsException;
import com.marketplace.identity.model.UserEntity;
import com.marketplace.identity.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final long TOKEN_EXPIRATION_SECONDS = 3600;

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail());

        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setUsername(username);
        userEntity.setEmail(email);
        userEntity.setPasswordHash(hashPassword(request.getPassword()));
        userEntity.setCreatedAt(Instant.now());

        try {
            userRepository.saveAndFlush(userEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("User already exists");
        }

        return new RegisterResponse(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getCreatedAt().toString());
    }

    public LoginResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!userEntity.getPasswordHash().equals(hashPassword(request.getPassword()))) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = "mock-jwt-" + userEntity.getUserId() + "-" + Instant.now().toEpochMilli();
        return new LoginResponse(token, "Bearer", TOKEN_EXPIRATION_SECONDS);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Password hashing algorithm unavailable", ex);
        }
    }
}
