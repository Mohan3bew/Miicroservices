package com.marketplace.identity.dto;

public class RegisterResponse {

    private final String userId;
    private final String username;
    private final String email;
    private final String createdAt;

    public RegisterResponse(String userId, String username, String email, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
