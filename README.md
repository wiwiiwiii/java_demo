# Customer Account Management System

A simple Java 17 classroom project for managing customer accounts. The program uses an English command-line menu, stores customers in a fixed array, and keeps all data in memory until the program exits.

This version is intentionally simple: every production class is in one package, method names describe the business action directly, and each audit requirement has one obvious code example.

## Project Structure

All production files are in `src/main/java/com/example/account/`.

| Class | Responsibility |
| --- | --- |
| `Main` | Starts the program and displays the English menus |
| `AccountSystem` | Stores customers and handles login, registration, add, delete, search, and filter operations |
| `Account` | Stores an account number and balance and returns its status |
| `AccountStatus` | Defines `ACTIVE` and `INACTIVE` |
| `AccountStatusRule` | Interface for deciding account status |
| `BalanceStatusRule` | Current rule: positive balance is active |
| `Customer` | Abstract parent class for shared customer information |
| `StandardCustomer` | Standard customer subclass |
| `PremiumCustomer` | Premium customer subclass |
| `CorporateCustomer` | Corporate customer subclass |
| `CustomerType` | Defines the three customer types |
| `AccountException` | One exception type for input and business errors |

The three focused test classes are `AccountTest`, `CustomerTest`, and `AccountSystemTest`.

## Run The Project

Requirements:

- Java 17 or later
- Maven 3.8 or later

```bash
mvn test
mvn package
java -jar target/account-array-demo-1.0-SNAPSHOT.jar
```

A real system terminal masks password input with `Console.readPassword()`. An IDE console may not support masking, so the program displays a warning before using visible fallback input.

## Demo Login

- Administrator username: `admin`
- Administrator password: `Admin123`
- Seeded customer password: `Password1`

These are public classroom credentials and must not be reused in a real system.

## Features

Guest users can:

- Log in
- Register a customer account
- Exit

Administrators can:

- List all customers
- Search by customer ID, name, username, or account number
- Filter by customer type, account status, or minimum balance
- Add a customer
- Delete a customer
- Log out or exit

Customers can view their own account and log out or exit. The system starts with five customers and stores up to twenty customers in one `Customer[]` array.

## Six Audit Requirements

### 1. Enum

`AccountStatus` replaces hard-coded status strings with `ACTIVE` and `INACTIVE`. `CustomerType` similarly defines `STANDARD`, `PREMIUM`, and `CORPORATE`.

### 2. Interface

`AccountStatusRule` contains the status business rule contract. `BalanceStatusRule` implements the current rule, and `Account` receives the rule through its constructor.

### 3. Inheritance

`Customer` is the abstract parent. `StandardCustomer`, `PremiumCustomer`, and `CorporateCustomer` inherit its common fields and methods and return their own `CustomerType`.

### 4. Exception Handling

All validation, login, permission, duplicate, capacity, and lookup errors use `AccountException`. `Main.run()` catches errors, prints an English message, and keeps the program running.

### 5. Lambda Expression

`AccountSystem.filterCustomers(Predicate<Customer>)` accepts flexible Lambda expressions. `Main.filterCustomers()` shows clear examples for type, status, and balance filters.

### 6. Unit Testing

- `AccountTest` covers Enum status, the interface rule, balance boundaries, and invalid accounts.
- `CustomerTest` covers the three inherited customer types, validation, and password behavior.
- `AccountSystemTest` covers five seeded customers, login/logout, registration, permissions, array operations, search, and Lambda filters.

Run all tests with `mvn test`.

## Suggested Demo Order

1. Run `mvn test` to show automated verification.
2. Open `AccountStatus` to explain Enum values.
3. Open `AccountStatusRule` and `BalanceStatusRule` to explain the interface.
4. Open `Customer` and its three subclasses to explain inheritance.
5. Open `AccountException` and the catch block in `Main.run()`.
6. Open `AccountSystem.filterCustomers()` and the Lambda examples in `Main`.
7. Run the JAR, log in as administrator, then demonstrate list, search, filter, add, and delete.

## Classroom Scope

This project is designed for explaining Java concepts, not for production banking. Data is not persisted, passwords are not hashed, authentication uses simple in-memory state, and there is no database, network API, Spring Boot, or graphical interface.
