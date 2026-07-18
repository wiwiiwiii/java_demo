package com.example.account.service;

import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.exception.AuthenticationException;
import com.example.account.exception.CustomerNotFoundException;
import com.example.account.exception.ValidationException;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.security.UserSession;

import java.util.Objects;

public final class AuthenticationService {
    private static final String AUTHENTICATION_FAILURE = "Invalid username or password";

    private final ArrayCustomerRepository repository;

    public AuthenticationService(ArrayCustomerRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public UserSession login(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || isBlank(password)) {
            throw authenticationFailure();
        }
        if ("admin".equals(username) && matchesAdminPassword(password)) {
            return UserSession.admin(username);
        }

        try {
            Customer customer = repository.findByUsername(username);
            if (customer.matchesPassword(password)) {
                return UserSession.customer(customer.getUsername(), customer.getCustomerId());
            }
        } catch (CustomerNotFoundException ignored) {
            // Authentication deliberately hides whether the username exists.
        }
        throw authenticationFailure();
    }

    public UserSession register(CustomerType type, String customerId, String name,
                                String username, char[] password, String accountNumber,
                                double balance) {
        if (type == null) {
            throw new ValidationException("Customer type must not be null");
        }
        Customer customer = CustomerFactory.create(type, customerId, name, username, password,
                accountNumber, balance);
        repository.save(customer);
        return UserSession.customer(customer.getUsername(), customer.getCustomerId());
    }

    private static AuthenticationException authenticationFailure() {
        return new AuthenticationException(AUTHENTICATION_FAILURE);
    }

    private static boolean isBlank(char[] value) {
        if (value.length == 0) {
            return true;
        }
        for (char character : value) {
            if (!Character.isWhitespace(character)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesAdminPassword(char[] candidate) {
        char[] expected = {'A', 'd', 'm', 'i', 'n', '1', '2', '3'};
        int difference = expected.length ^ candidate.length;
        int comparisonLength = Math.max(expected.length, candidate.length);
        for (int index = 0; index < comparisonLength; index++) {
            char expectedCharacter = index < expected.length ? expected[index] : 0;
            char actualCharacter = index < candidate.length ? candidate[index] : 0;
            difference |= expectedCharacter ^ actualCharacter;
        }
        return difference == 0;
    }
}
