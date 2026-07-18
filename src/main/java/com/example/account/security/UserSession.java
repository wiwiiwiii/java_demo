package com.example.account.security;

import com.example.account.domain.UserRole;
import com.example.account.exception.ValidationException;

import java.util.UUID;

public final class UserSession {
    private final String username;
    private final UserRole role;
    private final String customerId;
    private final UUID token;

    private UserSession(String username, UserRole role, String customerId) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username must not be blank");
        }
        if (role == null) {
            throw new ValidationException("Role must not be null");
        }
        if (role == UserRole.ADMIN && customerId != null) {
            throw new ValidationException("Administrator session must not have a customer ID");
        }
        if (role == UserRole.CUSTOMER && (customerId == null || customerId.isBlank())) {
            throw new ValidationException("Customer session must have a customer ID");
        }
        this.username = username;
        this.role = role;
        this.customerId = customerId;
        this.token = UUID.randomUUID();
    }

    public static UserSession admin(String username) {
        return new UserSession(username, UserRole.ADMIN, null);
    }

    public static UserSession customer(String username, String customerId) {
        return new UserSession(username, UserRole.CUSTOMER, customerId);
    }

    public String username() {
        return username;
    }

    public UserRole role() {
        return role;
    }

    public String customerId() {
        return customerId;
    }

    public UUID token() {
        return token;
    }

    @Override
    public String toString() {
        return "UserSession{username='" + username + "', role=" + role
                + ", customerId='" + customerId + "'}";
    }
}
