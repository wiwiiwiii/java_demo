package com.example.account.service;

import com.example.account.Account;
import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.domain.UserRole;
import com.example.account.exception.AuthorizationException;
import com.example.account.exception.ValidationException;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.security.UserSession;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public final class CustomerService {
    private final ArrayCustomerRepository repository;
    private final SessionRegistry sessions;

    public CustomerService(ArrayCustomerRepository repository, SessionRegistry sessions) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    public Customer[] listAll(UserSession session) {
        requireAdmin(session);
        return repository.findAll();
    }

    public Customer[] search(UserSession session, String query) {
        requireAdmin(session);
        if (query == null || query.isBlank()) {
            throw new ValidationException("Search query must not be blank");
        }
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        return Arrays.stream(repository.findAll())
                .filter(customer -> customer.getCustomerId().toLowerCase(Locale.ROOT)
                                .contains(normalizedQuery)
                        || customer.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || customer.getUsername().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || customer.getAccount().getId().toLowerCase(Locale.ROOT)
                                .contains(normalizedQuery))
                .toArray(Customer[]::new);
    }

    public Customer[] filter(UserSession session, Predicate<Customer> predicate) {
        requireAdmin(session);
        if (predicate == null) {
            throw new ValidationException("Customer predicate must not be null");
        }
        return Arrays.stream(repository.findAll())
                .filter(predicate)
                .toArray(Customer[]::new);
    }

    public Customer addCustomer(UserSession session, CustomerType type, String customerId,
                                String name, String username, char[] password,
                                String accountNumber, double balance) {
        requireAdmin(session);
        UsernamePolicy.requireNotReserved(username);
        Customer customer = CustomerFactory.create(type, customerId, name, username, password,
                accountNumber, balance);
        repository.save(customer);
        return customer;
    }

    public void deleteCustomer(UserSession session, String customerId) {
        requireAdmin(session);
        repository.deleteByCustomerId(customerId);
        sessions.invalidateCustomer(customerId);
    }

    public Account getOwnAccount(UserSession session) {
        if (!sessions.isActive(session) || session.role() != UserRole.CUSTOMER) {
            throw new AuthorizationException("Customer access required");
        }
        Customer customer = repository.findByCustomerId(session.customerId());
        if (!customer.getUsername().equals(session.username())) {
            throw new AuthorizationException("Customer session does not match customer record");
        }
        return customer.getAccount();
    }

    private void requireAdmin(UserSession session) {
        if (!sessions.isActive(session) || session.role() != UserRole.ADMIN) {
            throw new AuthorizationException("Administrator access required");
        }
    }
}
