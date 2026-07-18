package com.example.account.service;

import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.exception.AuthenticationException;
import com.example.account.exception.CustomerNotFoundException;
import com.example.account.exception.ValidationException;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.security.UserSession;

import java.util.Arrays;
import java.util.Objects;

public final class AuthenticationService {
    private static final String AUTHENTICATION_FAILURE = "Invalid username or password";

    private final ArrayCustomerRepository repository;
    private final SessionRegistry sessions;

    public AuthenticationService(ArrayCustomerRepository repository, SessionRegistry sessions) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    public UserSession login(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || isBlank(password)) {
            throw authenticationFailure();
        }
        if ("admin".equalsIgnoreCase(username)) {
            if (matchesAdminPassword(password)) {
                return sessions.issueAdmin(username);
            }
            throw authenticationFailure();
        }

        try {
            Customer customer = repository.findByUsername(username);
            if (customer.matchesPassword(password)) {
                return sessions.issueCustomer(customer.getUsername(), customer.getCustomerId());
            }
        } catch (CustomerNotFoundException ignored) {
            dummyPasswordComparison(password);
        }
        throw authenticationFailure();
    }

    public UserSession register(CustomerType type, String customerId, String name,
                                String username, char[] password, String accountNumber,
                                double balance) {
        if (type == null) {
            throw new ValidationException("Customer type must not be null");
        }
        UsernamePolicy.requireNotReserved(username);
        Customer customer = CustomerFactory.create(type, customerId, name, username, password,
                accountNumber, balance);
        repository.save(customer);
        return sessions.issueCustomer(customer.getUsername(), customer.getCustomerId());
    }

    public void logout(UserSession session) {
        sessions.invalidate(session);
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
        try {
            return constantTimeMatches(expected, candidate);
        } finally {
            Arrays.fill(expected, '\0');
        }
    }

    private static void dummyPasswordComparison(char[] candidate) {
        char[] dummy = {'N', 'o', 't', 'A', 'P', 'a', 's', 's'};
        try {
            constantTimeMatches(dummy, candidate);
        } finally {
            Arrays.fill(dummy, '\0');
        }
    }

    private static boolean constantTimeMatches(char[] expected, char[] candidate) {
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
