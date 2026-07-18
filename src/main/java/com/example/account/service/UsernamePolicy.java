package com.example.account.service;

import com.example.account.exception.ValidationException;

final class UsernamePolicy {
    private UsernamePolicy() {
    }

    static void requireNotReserved(String username) {
        if (username != null && "admin".equalsIgnoreCase(username)) {
            throw new ValidationException("Username is reserved: admin");
        }
    }
}
