package com.example.account.security;

import com.example.account.domain.UserRole;
import com.example.account.exception.ValidationException;

public record UserSession(String username, UserRole role, String customerId) {
    public UserSession {
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
    }

    public static UserSession admin(String username) {
        return new UserSession(username, UserRole.ADMIN, null);
    }

    public static UserSession customer(String username, String customerId) {
        return new UserSession(username, UserRole.CUSTOMER, customerId);
    }
}
