# Account Array Demo

A Java 17 classroom account-management application with an array-backed customer repository,
role-based authentication, customer search and filtering, and an interactive English console.
Data exists in memory for the life of the process; the application does not use a database or
file persistence.

## Prerequisites and commands

- Java 17 or later
- Apache Maven 3.8 or later

```bash
mvn test
mvn package
java -jar target/account-array-demo-1.0-SNAPSHOT.jar
```

The demonstration administrator credentials are `admin` / `Admin123`. They are intentionally
public classroom credentials and must not be reused in a real system. A real system terminal
masks password input. An IDE console often cannot mask it, so the application prints a warning
and uses visible fallback input there.

## Features

- Guest registration and administrator/customer login
- Administrator list, search, filter, add, and confirmed-delete operations
- Customer read-only access to their own account
- Five seeded customers spanning Standard, Premium, and Corporate tiers
- Active/inactive account status derived from a balance policy
- Fixed-capacity, in-memory array repository with duplicate protection

## Compliance mapping

1. **Classes and objects:** accounts, customers, sessions, services, and the application
   composition root have focused responsibilities.
2. **Encapsulation:** account state is private, credentials are defensively copied, and passwords
   are omitted from public string representations.
3. **Inheritance and polymorphism:** Standard, Premium, and Corporate customers share the abstract
   `Customer` contract while supplying their tier-specific type.
4. **Interfaces and abstraction:** `AccountStatusPolicy` isolates status rules and `ConsoleIO`
   isolates terminal input/output for production and tests.
5. **Arrays and exceptions:** `ArrayCustomerRepository` provides bounded array storage and reports
   validation, duplicate, capacity, lookup, authentication, and authorization failures explicitly.
6. **Enums, lambdas, and streams:** uppercase enums model roles, tiers, and statuses; predicates and
   stream operations support filtering, searching, and fixture compatibility.
