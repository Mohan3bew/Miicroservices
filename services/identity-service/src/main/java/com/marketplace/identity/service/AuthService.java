package com.marketplace.identity.service;

import com.marketplace.identity.dto.LoginRequest;
import com.marketplace.identity.dto.LoginResponse;
import com.marketplace.identity.dto.RegisterRequest;
import com.marketplace.identity.dto.RegisterResponse;
import com.marketplace.identity.exception.InvalidCredentialsException;
import com.marketplace.identity.exception.UserAlreadyExistsException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final long TOKEN_EXPIRATION_SECONDS = 3600;

    private final ConcurrentMap<String, UserRecord> usersByUsername = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> usernameByEmail = new ConcurrentHashMap<>();

    public RegisterResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail());

        if (usersByUsername.containsKey(username) || usernameByEmail.containsKey(email)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        UserRecord userRecord = new UserRecord(
                UUID.randomUUID().toString(),
                username,
                email,
                hashPassword(request.getPassword()),
                Instant.now());

        UserRecord existingUser = usersByUsername.putIfAbsent(username, userRecord);
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        String existingEmailUser = usernameByEmail.putIfAbsent(email, username);
        if (existingEmailUser != null) {
            usersByUsername.remove(username, userRecord);
            throw new UserAlreadyExistsException("User already exists");
        }

        return new RegisterResponse(
                userRecord.userId(),
                userRecord.username(),
                userRecord.email(),
                userRecord.createdAt().toString());
    }

    public LoginResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());
        UserRecord userRecord = usersByUsername.get(username);

        if (userRecord == null || !userRecord.passwordHash().equals(hashPassword(request.getPassword()))) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = "mock-jwt-" + userRecord.userId() + "-" + Instant.now().toEpochMilli();
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

    private record UserRecord(String userId, String username, String email, String passwordHash, Instant createdAt) {
    }
}
