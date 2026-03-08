package com.marketplace.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.marketplace.identity.dto.LoginRequest;
import com.marketplace.identity.dto.LoginResponse;
import com.marketplace.identity.dto.RegisterRequest;
import com.marketplace.identity.dto.RegisterResponse;
import com.marketplace.identity.exception.InvalidCredentialsException;
import com.marketplace.identity.exception.UserAlreadyExistsException;
import com.marketplace.identity.model.UserEntity;
import com.marketplace.identity.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;
    private Map<String, UserEntity> usersByUsername;
    private Map<String, UserEntity> usersByEmail;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
        usersByUsername = new HashMap<>();
        usersByEmail = new HashMap<>();

        when(userRepository.existsByUsernameOrEmail(anyString(), anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            String email = invocation.getArgument(1);
            return usersByUsername.containsKey(username) || usersByEmail.containsKey(email);
        });

        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            usersByUsername.put(user.getUsername(), user);
            usersByEmail.put(user.getEmail(), user);
            return user;
        });

        lenient().when(userRepository.findByUsername(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            return Optional.ofNullable(usersByUsername.get(username));
        });
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
