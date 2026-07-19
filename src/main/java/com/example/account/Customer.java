package com.example.account;

import java.util.Arrays;

public abstract class Customer {
    private final String customerId;
    private final String name;
    private final String username;
    private final char[] password;
    private final Account account;

    protected Customer(String customerId, String name, String username, char[] password,
                       Account account) {
        this.customerId = requireNonBlank(customerId, "Customer ID");
        this.name = requireNonBlank(name, "Name");
        this.username = requireNonBlank(username, "Username");
        if (password == null || password.length == 0 || isBlank(password)) {
            throw new AccountException("Password must not be blank");
        }
        this.password = Arrays.copyOf(password, password.length);
        if (account == null) {
            throw new AccountException("Account must not be null");
        }
        this.account = account;
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

    public Account getAccount() {
        return account;
    }

    public boolean matchesPassword(char[] candidate) {
        return candidate != null && Arrays.equals(password, candidate);
    }

    public abstract CustomerType getType();

    @Override
    public String toString() {
        return "Customer{customerId='" + customerId + "', name='" + name
                + "', username='" + username + "', type=" + getType()
                + ", account=" + account + "}";
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new AccountException(fieldName + " must not be blank");
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
