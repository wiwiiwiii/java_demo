package com.example.account;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final AccountSystem system = new AccountSystem();
    private boolean running = true;
    private boolean passwordWarningShown;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        while (running) {
            try {
                if (!system.isLoggedIn()) {
                    showGuestMenu();
                } else if (system.isAdminLoggedIn()) {
                    showAdminMenu();
                } else {
                    showCustomerMenu();
                }
            } catch (AccountException | NumberFormatException exception) {
                System.out.println("Error: " + exception.getMessage());
            } catch (IllegalArgumentException exception) {
                System.out.println("Error: Invalid option");
            }
        }
        system.logout();
        System.out.println("Goodbye.");
    }

    private void showGuestMenu() {
        System.out.println("\nGuest Menu");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        switch (readText("Choose an option: ")) {
            case "1" -> login();
            case "2" -> system.registerCustomer(readCustomer());
            case "3" -> running = false;
            default -> throw new AccountException("Invalid option");
        }
    }

    private void showAdminMenu() {
        System.out.println("\nAdministrator Menu");
        System.out.println("1. List customers");
        System.out.println("2. Search customers");
        System.out.println("3. Filter customers");
        System.out.println("4. Add customer");
        System.out.println("5. Delete customer");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
        switch (readText("Choose an option: ")) {
            case "1" -> printCustomers(system.getAllCustomers());
            case "2" -> printCustomers(system.searchCustomers(readText("Search: ")));
            case "3" -> filterCustomers();
            case "4" -> system.addCustomer(readCustomer());
            case "5" -> system.deleteCustomer(readText("Customer ID: "));
            case "6" -> system.logout();
            case "7" -> running = false;
            default -> throw new AccountException("Invalid option");
        }
    }

    private void showCustomerMenu() {
        System.out.println("\nCustomer Menu");
        printAccount(system.getCurrentCustomer().getAccount());
        System.out.println("1. Logout");
        System.out.println("2. Exit");
        switch (readText("Choose an option: ")) {
            case "1" -> system.logout();
            case "2" -> running = false;
            default -> throw new AccountException("Invalid option");
        }
    }

    private void login() {
        String username = readText("Username: ");
        char[] password = readPassword("Password: ");
        try {
            system.login(username, password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private Customer readCustomer() {
        CustomerType type = CustomerType.valueOf(readText("Type (STANDARD/PREMIUM/CORPORATE): ")
                .toUpperCase());
        String id = readText("Customer ID: ");
        String name = readText("Name: ");
        String username = readText("Username: ");
        char[] password = readPassword("Password: ");
        try {
            Account account = new Account(readText("Account number: "), readNumber("Balance: "));
            return switch (type) {
                case STANDARD -> new StandardCustomer(id, name, username, password, account);
                case PREMIUM -> new PremiumCustomer(id, name, username, password, account);
                case CORPORATE -> new CorporateCustomer(id, name, username, password, account);
            };
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void filterCustomers() {
        System.out.println("1. Customer type");
        System.out.println("2. Account status");
        System.out.println("3. Minimum balance");
        Customer[] result = switch (readText("Filter option: ")) {
            case "1" -> {
                CustomerType type = CustomerType.valueOf(readText("Type: ").toUpperCase());
                yield system.filterCustomers(customer -> customer.getType() == type);
            }
            case "2" -> {
                AccountStatus status = AccountStatus.valueOf(readText("Status: ").toUpperCase());
                yield system.filterCustomers(customer -> customer.getAccount().getStatus() == status);
            }
            case "3" -> {
                double minimum = readNumber("Minimum balance: ");
                yield system.filterCustomers(customer -> customer.getAccount().getBalance() >= minimum);
            }
            default -> throw new AccountException("Invalid filter option");
        };
        printCustomers(result);
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            running = false;
            throw new AccountException("Input ended");
        }
        return scanner.nextLine().trim();
    }

    private char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] value = console.readPassword("%s", prompt);
            return value == null ? new char[0] : value;
        }
        if (!passwordWarningShown) {
            System.out.println("Warning: password input is visible in this console.");
            passwordWarningShown = true;
        }
        return readText(prompt).toCharArray();
    }

    private double readNumber(String prompt) {
        return Double.parseDouble(readText(prompt));
    }

    private void printCustomers(Customer[] customers) {
        if (customers.length == 0) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer customer : customers) {
            System.out.println(customer);
        }
    }

    private void printAccount(Account account) {
        System.out.printf("Account: %s | Balance: %.2f | Status: %s%n",
                account.getAccountNumber(), account.getBalance(), account.getStatus());
    }
}
