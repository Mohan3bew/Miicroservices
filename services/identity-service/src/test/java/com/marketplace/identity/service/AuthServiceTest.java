package com.marketplace.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marketplace.identity.dto.LoginRequest;
import com.marketplace.identity.dto.LoginResponse;
import com.marketplace.identity.dto.RegisterRequest;
import com.marketplace.identity.dto.RegisterResponse;
import com.marketplace.identity.exception.InvalidCredentialsException;
import com.marketplace.identity.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    void shouldRegisterAndLoginUser() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("demo");
        registerRequest.setEmail("demo@example.com");
        registerRequest.setPassword("password123");

        RegisterResponse registerResponse = authService.register(registerRequest);

        assertNotNull(registerResponse.getUserId());
        assertEquals("demo", registerResponse.getUsername());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("demo");
        loginRequest.setPassword("password123");

        LoginResponse loginResponse = authService.login(loginRequest);

        assertEquals("Bearer", loginResponse.getTokenType());
        assertEquals(3600, loginResponse.getExpiresIn());
        assertTrue(loginResponse.getAccessToken().startsWith("mock-jwt-"));
    }

    @Test
    void shouldRejectDuplicateUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("password123");

        authService.register(request);

        RegisterRequest duplicate = new RegisterRequest();
        duplicate.setUsername("demo");
        duplicate.setEmail("other@example.com");
        duplicate.setPassword("password123");

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(duplicate));
    }

    @Test
    void shouldRejectInvalidLoginCredentials() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("password123");

        authService.register(request);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("demo");
        loginRequest.setPassword("wrong-password");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }
}
