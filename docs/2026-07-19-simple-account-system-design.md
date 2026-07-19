# Simple Customer Account System Design

## Goal

Refactor the current customer account management application into a classroom-friendly Java project that is easy to explain. Keep the existing English command-line features and all six audit requirements, while removing security and architecture mechanisms that are beyond the intended learning scope.

The application remains an in-memory Maven/Java 17 project. It does not use Spring Boot, a GUI, a database, file persistence, deposits, withdrawals, or transfers.

## Simplified Structure

All production classes live directly in `com.example.account` so a learner can read the project in one directory:

- `Main`: application entry point and English menu interaction.
- `AccountSystem`: customer array, current login state, and customer-management operations.
- `Account`: account number, balance, and account status.
- `AccountStatus`: `ACTIVE` and `INACTIVE` enum values.
- `AccountStatusRule`: interface for the account-status business rule.
- `BalanceStatusRule`: current positive-balance status implementation.
- `Customer`: abstract base class for common customer data and password checking.
- `StandardCustomer`, `PremiumCustomer`, `CorporateCustomer`: inheritance examples.
- `CustomerType`: customer-tier enum.
- `AccountException`: one application exception for validation, authentication, authorization, lookup, duplicate, and capacity errors.

The refactor removes the current package layers and deletes `SessionRegistry`, `UserSession`, `AuthenticationService`, `CustomerService`, `ArrayCustomerRepository`, `CustomerFactory`, `ConsoleController`, `ConsoleIO`, `SystemConsoleIO`, `UsernamePolicy`, and the multiple specialized exception classes.

## Data And Login Model

`AccountSystem` owns one fixed-capacity `Customer[]`, initialized with exactly five customers covering all three customer types. Data exists only for the current process.

The system stores a simple `adminLoggedIn` boolean and a `currentCustomer` reference. The administrator uses the documented classroom credentials `admin` / `Admin123`. Customer login searches the array by username and checks the password. Logout clears both login fields.

Passwords remain `char[]` values and are excluded from display output. `Main` uses `System.console().readPassword()` in a real terminal. When no system console is available, it prints an English warning and reads visible fallback input with `Scanner`.

This is a classroom authentication model, not a production security design. Session registries, token provenance, timing defenses, and cross-composition session validation are intentionally removed.

## Features

The guest menu contains `Login`, `Register`, and `Exit`.

The administrator menu contains list, search, filter, add, delete, logout, and exit operations. The customer menu displays only the current customer's account and supports logout and exit.

`AccountSystem` exposes straightforward methods:

- `login`
- `registerCustomer`
- `logout`
- `addCustomer`
- `deleteCustomer`
- `getAllCustomers`
- `searchCustomers`
- `filterCustomers`
- `getCurrentCustomer`
- `isAdminLoggedIn`

Customer registration and administrator addition collect type, customer ID, name, username, password, account number, and initial balance. Duplicate IDs, usernames, and account numbers are rejected. Deletion compacts the customer array. Search uses a loop and matches ID, name, username, or account number. Filtering accepts `Predicate<Customer>` and uses Lambda expressions or streams.

All input and business errors become `AccountException` messages. `Main` catches the exception, prints an English message, and continues at the appropriate menu.

## Six Audit Requirements

1. **Enum:** `AccountStatus` standardizes `ACTIVE` and `INACTIVE`. `CustomerType` standardizes customer tiers.
2. **Interface:** `AccountStatusRule` separates the balance-status rule from `Account`; `BalanceStatusRule` provides the current rule.
3. **Inheritance:** `Customer` is the common abstract parent of Standard, Premium, and Corporate customers.
4. **Exception handling:** `AccountException` represents invalid input and business failures; `Main` handles it without terminating the program.
5. **Lambda expression:** `AccountSystem.filterCustomers(Predicate<Customer>)` provides flexible type, status, and balance filtering.
6. **Unit testing:** focused JUnit tests cover normal behavior, zero-balance boundaries, invalid values, exception behavior, login, registration, permissions, array operations, search, and Lambda filtering.

## Testing

The test suite is reduced to approximately 20-30 focused tests:

- `AccountTest`: enum status, replaceable interface rule, boundaries, and invalid account input.
- `CustomerTest`: three inherited types, password matching, validation, and password-free display.
- `AccountSystemTest`: initial five customers, login/logout, registration, administrator operations, permissions, duplicates, capacity, search, and Lambda filters.

The command-line menu is verified with a small launch smoke test rather than a large scripted console framework. Completion requires `mvn test`, `mvn package`, and a successful executable-JAR exit flow.

## README

The README will be rewritten around classroom presentation:

1. Project purpose
2. Simple directory and class map
3. Build and run commands
4. Demo administrator credentials
5. Guest, administrator, and customer menu features
6. Direct mapping of each audit requirement to one class or method
7. A short recommended demonstration order

The README will explicitly state that this is a simplified classroom system and not a production authentication design.
