package com.example.account;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

public class AccountSystem {
    private static final int CAPACITY = 20;
    private static final String ADMIN_USERNAME = "admin";
    private static final char[] ADMIN_PASSWORD = "Admin123".toCharArray();

    private final Customer[] customers = new Customer[CAPACITY];
    private int customerCount;
    private boolean adminLoggedIn;
    private Customer currentCustomer;

    public AccountSystem() {
        addToArray(new StandardCustomer("C001", "Alice", "alice", password(),
                new Account("A001", 500)));
        addToArray(new PremiumCustomer("C002", "Bob", "bob", password(),
                new Account("A002", 10_000)));
        addToArray(new CorporateCustomer("C003", "Acme Ltd", "acme", password(),
                new Account("A003", 15_000)));
        addToArray(new StandardCustomer("C004", "Diana", "diana", password(),
                new Account("A004", 0)));
        addToArray(new PremiumCustomer("C005", "Evan", "evan", password(),
                new Account("A005", 8_000)));
    }

    public void login(String username, char[] password) {
        if (ADMIN_USERNAME.equals(username) && Arrays.equals(ADMIN_PASSWORD, password)) {
            adminLoggedIn = true;
            currentCustomer = null;
            return;
        }
        Customer customer = findByUsername(username);
        if (customer == null || !customer.matchesPassword(password)) {
            throw new AccountException("Invalid username or password");
        }
        adminLoggedIn = false;
        currentCustomer = customer;
    }

    public Customer registerCustomer(Customer customer) {
        if (isLoggedIn()) {
            throw new AccountException("Logout before registering a customer");
        }
        checkDuplicate(customer);
        return addToArray(customer);
    }

    public Customer addCustomer(Customer customer) {
        requireAdmin();
        checkDuplicate(customer);
        return addToArray(customer);
    }

    public void deleteCustomer(String customerId) {
        requireAdmin();
        int index = -1;
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getCustomerId().equals(customerId)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new AccountException("Customer not found");
        }
        for (int i = index; i < customerCount - 1; i++) {
            customers[i] = customers[i + 1];
        }
        customers[--customerCount] = null;
    }

    public Customer[] getAllCustomers() {
        return Arrays.copyOf(customers, customerCount);
    }

    public Customer[] searchCustomers(String query) {
        requireAdmin();
        if (query == null || query.isBlank()) {
            throw new AccountException("Search query is required");
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        Customer[] matches = new Customer[customerCount];
        int matchCount = 0;
        for (int i = 0; i < customerCount; i++) {
            Customer customer = customers[i];
            if (contains(customer.getCustomerId(), normalized)
                    || contains(customer.getName(), normalized)
                    || contains(customer.getUsername(), normalized)
                    || contains(customer.getAccount().getAccountNumber(), normalized)) {
                matches[matchCount++] = customer;
            }
        }
        return Arrays.copyOf(matches, matchCount);
    }

    public Customer[] filterCustomers(Predicate<Customer> condition) {
        requireAdmin();
        if (condition == null) {
            throw new AccountException("Filter is required");
        }
        return Arrays.stream(getAllCustomers()).filter(condition).toArray(Customer[]::new);
    }

    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    public boolean isLoggedIn() {
        return adminLoggedIn || currentCustomer != null;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public void logout() {
        adminLoggedIn = false;
        currentCustomer = null;
    }

    private Customer addToArray(Customer customer) {
        if (customer == null) {
            throw new AccountException("Customer is required");
        }
        if (customerCount == CAPACITY) {
            throw new AccountException("Customer capacity reached");
        }
        customers[customerCount++] = customer;
        return customer;
    }

    private Customer findByUsername(String username) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getUsername().equals(username)) {
                return customers[i];
            }
        }
        return null;
    }

    private void checkDuplicate(Customer candidate) {
        if (candidate == null) {
            throw new AccountException("Customer is required");
        }
        if (ADMIN_USERNAME.equalsIgnoreCase(candidate.getUsername())) {
            throw new AccountException("Username admin is reserved");
        }
        for (int i = 0; i < customerCount; i++) {
            Customer existing = customers[i];
            if (existing.getCustomerId().equals(candidate.getCustomerId())) {
                throw new AccountException("Duplicate customer ID");
            }
            if (existing.getUsername().equals(candidate.getUsername())) {
                throw new AccountException("Duplicate username");
            }
            if (existing.getAccount().getAccountNumber()
                    .equals(candidate.getAccount().getAccountNumber())) {
                throw new AccountException("Duplicate account number");
            }
        }
    }

    private void requireAdmin() {
        if (!adminLoggedIn) {
            throw new AccountException("Administrator login required");
        }
    }

    private static boolean contains(String value, String normalizedQuery) {
        return value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private static char[] password() {
        return "Password1".toCharArray();
    }
}
