package com.example.account.service;

import com.example.account.Account;
import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.domain.UserRole;
import com.example.account.exception.AuthorizationException;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.security.UserSession;

import java.util.Objects;

public final class CustomerService {
    private final ArrayCustomerRepository repository;

    public CustomerService(ArrayCustomerRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public Customer[] listAll(UserSession session) {
        requireAdmin(session);
        return repository.findAll();
    }

    public Customer addCustomer(UserSession session, CustomerType type, String customerId,
                                String name, String username, char[] password,
                                String accountNumber, double balance) {
        requireAdmin(session);
        Customer customer = CustomerFactory.create(type, customerId, name, username, password,
                accountNumber, balance);
        repository.save(customer);
        return customer;
    }

    public void deleteCustomer(UserSession session, String customerId) {
        requireAdmin(session);
        repository.deleteByCustomerId(customerId);
    }

    public Account getOwnAccount(UserSession session) {
        if (session == null || session.role() != UserRole.CUSTOMER) {
            throw new AuthorizationException("Customer access required");
        }
        Customer customer = repository.findByCustomerId(session.customerId());
        if (!customer.getUsername().equals(session.username())) {
            throw new AuthorizationException("Customer session does not match customer record");
        }
        return customer.getAccount();
    }

    private static void requireAdmin(UserSession session) {
        if (session == null || session.role() != UserRole.ADMIN) {
            throw new AuthorizationException("Administrator access required");
        }
    }
}
