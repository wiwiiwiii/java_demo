package com.example.account;

import com.example.account.console.ConsoleController;
import com.example.account.console.ConsoleIO;
import com.example.account.console.SystemConsoleIO;
import com.example.account.customer.Customer;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.service.AuthenticationService;
import com.example.account.service.CustomerService;
import com.example.account.service.SessionRegistry;

import java.util.Objects;

public final class AccountManagementApplication {
    private static final int REPOSITORY_CAPACITY = 100;

    private AccountManagementApplication() {
    }

    public static ConsoleController createController(ConsoleIO io) {
        Objects.requireNonNull(io, "io");
        ArrayCustomerRepository repository = new ArrayCustomerRepository(REPOSITORY_CAPACITY);
        for (Customer customer : AccountDemo.createCustomers()) {
            repository.save(customer);
        }

        SessionRegistry sessions = new SessionRegistry();
        AuthenticationService authentication = new AuthenticationService(repository, sessions);
        CustomerService customers = new CustomerService(repository, sessions);
        return new ConsoleController(io, authentication, customers);
    }

    public static void main(String[] args) {
        createController(new SystemConsoleIO()).run();
    }
}
