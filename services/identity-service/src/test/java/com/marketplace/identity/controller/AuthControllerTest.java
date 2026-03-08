package com.marketplace.identity.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marketplace.identity.dto.LoginResponse;
import com.marketplace.identity.dto.RegisterResponse;
import com.marketplace.identity.exception.GlobalExceptionHandler;
import com.marketplace.identity.exception.InvalidCredentialsException;
import com.marketplace.identity.exception.UserAlreadyExistsException;
import com.marketplace.identity.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void healthShouldReturnUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("identity-service"));
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        when(authService.register(any())).thenReturn(new RegisterResponse(
                "user-1",
                "demo",
                "demo@example.com",
                "2026-03-08T00:00:00Z"));

        String payload = """
                {
                  "username": "demo",
                  "email": "demo@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.username").value("demo"));
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("token-value", "Bearer", 3600));

        String payload = """
                {
                  "username": "demo",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("token-value"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void loginShouldReturnBadRequestWhenPayloadInvalid() throws Exception {
        String payload = """
                {
                  "username": "demo",
                  "password": "short"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("password")));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenCredentialsInvalid() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException("Invalid username or password"));

        String payload = """
                {
                  "username": "demo",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void registerShouldReturnConflictForDuplicateUser() throws Exception {
        when(authService.register(any())).thenThrow(new UserAlreadyExistsException("User already exists"));

        String payload = """
                {
                  "username": "demo",
                  "email": "demo@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }
}
