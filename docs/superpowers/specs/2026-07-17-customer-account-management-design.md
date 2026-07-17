# Customer Account Management Compliance Upgrade Design

## Objective

Upgrade the existing ABC Bank customer account demo with minimal structural changes while preserving its original behavior: store five initial customers in an array, display their account details, and derive account status from balance. The upgraded application will use an English command-line interface and satisfy all six requirements in `Emergency_Scenario.pdf`.

The implementation must remove unnecessary `final` class declarations and replace lowercase hard-coded status strings with the `AccountStatus` enum values `ACTIVE` and `INACTIVE`. It will not use Spring Boot, a graphical frontend, a database, or persistent storage.

## Architecture

The application uses a small layered design:

- `AccountManagementApplication` assembles dependencies and starts the application.
- `ConsoleController` owns the English menus, input parsing, password entry, confirmation prompts, and user-facing error messages.
- `AuthenticationService` handles registration, login, logout state, and role checks.
- `CustomerService` handles listing, searching, filtering, adding, deleting, and access control.
- `ArrayCustomerRepository` stores customers in a fixed-capacity `Customer[]` and tracks its logical size.
- Domain classes represent accounts, credentials, roles, customer types, and statuses.

The repository begins with exactly five customer records, preserving the original array requirement. Its fixed capacity is 100 so the application can support additions during the current process. Data resets to the five initial records whenever the application restarts.

## Domain Model

`Customer` is an abstract base class containing common customer identity, account, username, and password credential behavior. `StandardCustomer`, `PremiumCustomer`, and `CorporateCustomer` extend it and expose their respective `CustomerType` value. Services operate on `Customer`, so all three types are handled polymorphically.

`Account` contains the account number, owner name, and balance. Its status is returned as `AccountStatus`, never as a hard-coded string. A positive balance produces `ACTIVE`; zero produces `INACTIVE`. Negative initial balances are invalid and rejected.

`UserRole` distinguishes `ADMIN` from `CUSTOMER`. The application includes one documented demo administrator. Registration always creates a `CUSTOMER` role and its associated customer account. Passwords are excluded from account displays, search results, and string representations.

## Audit Requirements

1. **Enum:** `AccountStatus` standardizes `ACTIVE` and `INACTIVE`; `CustomerType` and `UserRole` also prevent uncontrolled business-state strings.
2. **Interface:** `AccountStatusPolicy` separates the balance-status rule from `Account`. `BalanceBasedAccountStatusPolicy` supplies the current policy and can be replaced without changing the account class.
3. **Inheritance:** the abstract `Customer` base and its Standard, Premium, and Corporate subclasses provide polymorphic customer processing.
4. **Exception handling:** domain and service validation throw focused application exceptions for blank names, invalid account numbers, negative balances, duplicate usernames, failed authentication, missing customers, capacity exhaustion, and access violations. The controller catches them, prints an English message, and keeps the menu running.
5. **Lambda expressions:** `CustomerService` accepts predicates for flexible searches. Menu filters for type, status, and minimum balance are composed with lambdas and evaluated through the array-backed repository.
6. **Unit testing:** automated tests cover normal cases, boundary cases, invalid input, exception handling, authorization, filters, and representative console flows.

## Console Flow

The unauthenticated menu contains `Login`, `Register`, and `Exit`.

Registration asks for username, password, customer name, account number, initial balance, and customer type. Password entry uses `Console.readPassword()` in a real terminal. Input is provided through an adapter so tests can inject scripted input. If no system console is available, the application warns that masking is unavailable and recommends running in a system terminal; this limitation is not silently presented as secure masking.

The administrator menu contains:

1. List all customers
2. Search customers
3. Filter customers
4. Add customer
5. Delete customer
6. Logout
7. Exit

Search matches customer ID, customer name, username, or account number. Filters support customer type, account status, and minimum balance. Deletion requires confirmation. Administrators cannot delete the built-in administrator because it is not a customer record.

The customer menu shows only the authenticated customer's account and offers `Logout` and `Exit`. A customer cannot list, add, edit, or delete other customers.

## Validation And Recovery

Names and usernames must be nonblank. Usernames and account numbers must be unique. Account numbers use the format `A` followed by at least three digits. Initial balances must be finite and nonnegative. Passwords must be nonblank. Numeric and menu parsing failures remain in the controller and result in an English retry message rather than process termination.

Domain exceptions cross service boundaries; user-facing output does not expose stack traces or internal implementation details. Unexpected runtime failures are handled at the application boundary with a generic English error and a safe return to the appropriate menu where possible.

## Testing Strategy

Implementation follows red-green-refactor cycles. Tests will cover:

- positive and zero balance status boundaries;
- rejection of negative and non-finite balances;
- replacement of `AccountStatusPolicy` in a test;
- common polymorphic behavior for all customer subclasses;
- repository capacity and uniqueness rules;
- successful and failed registration and authentication;
- administrator and customer authorization boundaries;
- searching and lambda filters by type, status, and balance;
- console login, registration, add, delete, logout, exit, invalid choice, and invalid numeric input flows;
- password exclusion from all display representations.

Completion requires a fresh successful full test run and Maven package build.

## Compatibility And Scope

The existing account display behavior and five initial records remain available through the new menu. The old print-only entry point may delegate to the new application or remain as a compatibility fixture, but production interaction will no longer depend on printing the array and exiting.

No deposit, withdrawal, transfer, editing, password reset, file persistence, database, web server, or graphical interface is included.
