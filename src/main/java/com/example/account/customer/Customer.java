package com.example.account.customer;

import com.example.account.Account;
import com.example.account.domain.CustomerType;
import com.example.account.domain.UserRole;
import com.example.account.exception.ValidationException;

import java.util.Arrays;

public abstract class Customer {
    private final String customerId;
    private final String name;
    private final String username;
    private final char[] password;
    private final Account account;

    protected Customer(String customerId, String name, String username, char[] password,
                       String accountNumber, double balance) {
        this.customerId = requireNonBlank(customerId, "Customer ID");
        this.name = requireNonBlank(name, "Name");
        this.username = requireNonBlank(username, "Username");
        if (password == null || password.length == 0 || isBlank(password)) {
            throw new ValidationException("Password must not be blank");
        }
        this.password = Arrays.copyOf(password, password.length);
        this.account = new Account(accountNumber, name, balance);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public boolean matchesPassword(char[] candidate) {
        if (candidate == null) {
            return false;
        }
        int difference = password.length ^ candidate.length;
        int comparisonLength = Math.max(password.length, candidate.length);
        for (int index = 0; index < comparisonLength; index++) {
            char expected = index < password.length ? password[index] : 0;
            char actual = index < candidate.length ? candidate[index] : 0;
            difference |= expected ^ actual;
        }
        return difference == 0;
    }

    public Account getAccount() {
        return account;
    }

    public abstract CustomerType getType();

    public UserRole getRole() {
        return UserRole.CUSTOMER;
    }

    @Override
    public String toString() {
        return "Customer{customerId='" + customerId + "', name='" + name
                + "', username='" + username + "', type=" + getType()
                + ", account=" + account + "}";
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value;
    }

    private static boolean isBlank(char[] value) {
        for (char character : value) {
            if (!Character.isWhitespace(character)) {
                return false;
            }
        }
        return true;
    }
}
