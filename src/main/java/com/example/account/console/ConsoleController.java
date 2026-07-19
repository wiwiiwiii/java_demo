package com.example.account.console;

import com.example.account.Account;
import com.example.account.customer.Customer;
import com.example.account.AccountStatus;
import com.example.account.domain.CustomerType;
import com.example.account.domain.UserRole;
import com.example.account.exception.AuthenticationException;
import com.example.account.exception.AuthorizationException;
import com.example.account.security.UserSession;
import com.example.account.service.AuthenticationService;
import com.example.account.service.CustomerService;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public final class ConsoleController {
    private final ConsoleIO io;
    private final AuthenticationService authentication;
    private final CustomerService customers;
    private UserSession currentSession;
    private boolean running;

    public ConsoleController(ConsoleIO io, AuthenticationService authentication,
                             CustomerService customers) {
        this.io = Objects.requireNonNull(io, "io");
        this.authentication = Objects.requireNonNull(authentication, "authentication");
        this.customers = Objects.requireNonNull(customers, "customers");
    }

    public void run() {
        running = true;
        try {
            while (running) {
                try {
                    if (currentSession == null) {
                        guestMenu();
                    } else if (currentSession.role() == UserRole.ADMIN) {
                        administratorMenu();
                    } else {
                        customerMenu();
                    }
                } catch (InputClosedException exception) {
                    io.println("Input closed. Goodbye.");
                    running = false;
                } catch (AuthenticationException | AuthorizationException exception) {
                    io.println("Error: " + safeMessage(exception));
                    logoutSilently();
                } catch (RuntimeException exception) {
                    io.println("Error: " + safeMessage(exception));
                }
            }
        } finally {
            logoutSilently();
        }
    }

    private void guestMenu() {
        io.println("Guest Menu");
        io.println("1 Login");
        io.println("2 Register");
        io.println("3 Exit");
        switch (line("Choose an option: ")) {
            case "1" -> login();
            case "2" -> register();
            case "3" -> exit();
            default -> io.println("Invalid option.");
        }
    }

    private void administratorMenu() {
        io.println("Administrator Menu");
        io.println("1 List all customers");
        io.println("2 Search customers");
        io.println("3 Filter customers");
        io.println("4 Add customer");
        io.println("5 Delete customer");
        io.println("6 Logout");
        io.println("7 Exit");
        switch (line("Choose an option: ")) {
            case "1" -> printCustomers(customers.listAll(currentSession));
            case "2" -> printCustomers(customers.search(currentSession, line("Search query: ")));
            case "3" -> filterCustomers();
            case "4" -> addCustomer();
            case "5" -> deleteCustomer();
            case "6" -> logout();
            case "7" -> exit();
            default -> io.println("Invalid option.");
        }
    }

    private void customerMenu() {
        io.println("Customer Menu");
        printAccount(customers.getOwnAccount(currentSession));
        io.println("1 Logout");
        io.println("2 Exit");
        switch (line("Choose an option: ")) {
            case "1" -> logout();
            case "2" -> exit();
            default -> io.println("Invalid option.");
        }
    }

    private void login() {
        String username = line("Username: ");
        char[] password = password("Password: ");
        try {
            currentSession = authentication.login(username, password);
            io.println("Login successful.");
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void register() {
        CustomerInput input = readCustomerInput();
        try {
            currentSession = authentication.register(input.type(), input.customerId(), input.name(),
                    input.username(), input.password(), input.accountNumber(), input.balance());
            io.println("Registration successful. You are now logged in.");
        } finally {
            input.clearPassword();
        }
    }

    private void addCustomer() {
        CustomerInput input = readCustomerInput();
        try {
            customers.addCustomer(currentSession, input.type(), input.customerId(), input.name(),
                    input.username(), input.password(), input.accountNumber(), input.balance());
            io.println("Customer added.");
        } finally {
            input.clearPassword();
        }
    }

    private CustomerInput readCustomerInput() {
        String type = line("Customer type (STANDARD/PREMIUM/CORPORATE): ");
        String customerId = line("Customer ID: ");
        String name = line("Name: ");
        String username = line("Username: ");
        char[] password = password("Password: ");
        try {
            String accountNumber = line("Account number: ");
            String balance = line("Initial balance: ");
            return new CustomerInput(parseEnum(CustomerType.class, type, "customer type"),
                    customerId, name, username, password, accountNumber,
                    parseNumber(balance));
        } catch (RuntimeException exception) {
            Arrays.fill(password, '\0');
            throw exception;
        }
    }

    private void filterCustomers() {
        io.println("1 Customer type");
        io.println("2 Account status");
        io.println("3 Minimum balance");
        Predicate<Customer> predicate = switch (line("Choose a filter: ")) {
            case "1" -> {
                CustomerType type = parseEnum(CustomerType.class,
                        line("Customer type (STANDARD/PREMIUM/CORPORATE): "), "customer type");
                yield customer -> customer.getType() == type;
            }
            case "2" -> {
                AccountStatus status = parseEnum(AccountStatus.class,
                        line("Account status (ACTIVE/INACTIVE): "), "account status");
                yield customer -> customer.getAccount().getStatus() == status;
            }
            case "3" -> {
                double minimum = parseNumber(line("Minimum balance: "));
                yield customer -> customer.getAccount().getBalance() >= minimum;
            }
            default -> throw new IllegalArgumentException("Invalid filter option.");
        };
        printCustomers(customers.filter(currentSession, predicate));
    }

    private void deleteCustomer() {
        String customerId = line("Customer ID to delete: ");
        String confirmation = line("Confirm deletion (y/n): ");
        if (!"y".equalsIgnoreCase(confirmation)) {
            io.println("Deletion cancelled.");
            return;
        }
        customers.deleteCustomer(currentSession, customerId);
        io.println("Customer deleted.");
    }

    private void logout() {
        authentication.logout(currentSession);
        currentSession = null;
        io.println("Logged out.");
    }

    private void exit() {
        logoutSilently();
        io.println("Goodbye.");
        running = false;
    }

    private void logoutSilently() {
        if (currentSession != null) {
            try {
                authentication.logout(currentSession);
            } catch (RuntimeException ignored) {
                // Exit must remain clean even if a session was already invalidated.
            } finally {
                currentSession = null;
            }
        }
    }

    private void printCustomers(Customer[] matches) {
        if (matches.length == 0) {
            io.println("No customers found.");
            return;
        }
        io.println("ID | Name | Username | Type | Account | Balance | Status");
        for (Customer customer : matches) {
            Account account = customer.getAccount();
            io.println(customer.getCustomerId() + " | " + customer.getName() + " | "
                    + customer.getUsername() + " | " + customer.getType() + " | "
                    + account.getId() + " | " + account.getBalance() + " | "
                    + account.getStatus());
        }
    }

    private void printAccount(Account account) {
        io.println("Account: " + account.getId() + " | Owner: " + account.getOwner()
                + " | Balance: " + account.getBalance() + " | Status: "
                + account.getStatus());
    }

    private String line(String prompt) {
        String value = io.readLine(prompt);
        if (value == null) {
            throw new InputClosedException();
        }
        return value.trim();
    }

    private char[] password(String prompt) {
        char[] value = io.readPassword(prompt);
        if (value == null) {
            throw new InputClosedException();
        }
        return value;
    }

    private static double parseNumber(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid number.");
        }
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String value, String label) {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + label + ".");
        }
    }

    private static String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "Operation failed." : message;
    }

    private record CustomerInput(CustomerType type, String customerId, String name,
                                 String username, char[] password, String accountNumber,
                                 double balance) {
        void clearPassword() {
            Arrays.fill(password, '\0');
        }
    }

    private static final class InputClosedException extends RuntimeException {
    }
}
