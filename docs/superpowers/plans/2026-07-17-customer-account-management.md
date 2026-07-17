# Customer Account Management Compliance Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the print-only account demo into an English, array-backed command-line customer management application satisfying all six audit requirements.

**Architecture:** Keep the Maven project and existing account behavior, but separate domain rules, array storage, services, and console I/O. Authentication and customer operations use role checks, predicates provide flexible filtering, and all invalid input is converted into recoverable English console messages.

**Tech Stack:** Java 17, Maven, JUnit Jupiter 5.10.2, standard Java Console/Stream APIs only

---

## File Structure

- `src/main/java/com/example/account/domain/AccountStatus.java`: standardized status enum.
- `src/main/java/com/example/account/domain/CustomerType.java`: Standard/Premium/Corporate enum.
- `src/main/java/com/example/account/domain/UserRole.java`: administrator/customer authorization enum.
- `src/main/java/com/example/account/policy/AccountStatusPolicy.java`: replaceable status rule.
- `src/main/java/com/example/account/policy/BalanceBasedAccountStatusPolicy.java`: current positive-balance rule.
- `src/main/java/com/example/account/Account.java`: validated account model using the policy and enum.
- `src/main/java/com/example/account/customer/Customer.java`: abstract common customer behavior.
- `src/main/java/com/example/account/customer/{Standard,Premium,Corporate}Customer.java`: polymorphic customer tiers.
- `src/main/java/com/example/account/customer/CustomerFactory.java`: construct a subtype from `CustomerType`.
- `src/main/java/com/example/account/exception/*.java`: focused domain/service exceptions.
- `src/main/java/com/example/account/repository/ArrayCustomerRepository.java`: fixed-capacity array storage.
- `src/main/java/com/example/account/security/UserSession.java`: authenticated identity and role.
- `src/main/java/com/example/account/service/AuthenticationService.java`: registration and login.
- `src/main/java/com/example/account/service/CustomerService.java`: authorization, CRUD, search, and filtering.
- `src/main/java/com/example/account/console/ConsoleIO.java`: testable console abstraction.
- `src/main/java/com/example/account/console/SystemConsoleIO.java`: terminal password masking and stream fallback.
- `src/main/java/com/example/account/console/ConsoleController.java`: English menus and recovery loops.
- `src/main/java/com/example/account/AccountManagementApplication.java`: production composition root.
- `src/main/java/com/example/account/AccountDemo.java`: compatibility factory delegated to the new model.
- `src/test/java/com/example/account/**`: unit, service, repository, and console tests.

### Task 1: Enum-Based Account Status Policy

**Files:**
- Create: `src/main/java/com/example/account/domain/AccountStatus.java`
- Create: `src/main/java/com/example/account/policy/AccountStatusPolicy.java`
- Create: `src/main/java/com/example/account/policy/BalanceBasedAccountStatusPolicy.java`
- Create: `src/main/java/com/example/account/exception/ValidationException.java`
- Modify: `src/main/java/com/example/account/Account.java`
- Modify: `src/test/java/com/example/account/AccountTest.java`

- [ ] **Step 1: Write failing enum, boundary, validation, and policy replacement tests**

```java
@Test void positiveBalanceIsActive() {
    assertEquals(AccountStatus.ACTIVE, new Account("A001", "Alice", 1.0).getStatus());
}
@Test void zeroBalanceIsInactive() {
    assertEquals(AccountStatus.INACTIVE, new Account("A002", "Bob", 0.0).getStatus());
}
@Test void negativeAndNonFiniteBalancesAreRejected() {
    assertThrows(ValidationException.class, () -> new Account("A003", "Cara", -0.01));
    assertThrows(ValidationException.class, () -> new Account("A004", "Dan", Double.NaN));
}
@Test void injectedPolicyCanChangeStatusWithoutChangingAccount() {
    AccountStatusPolicy alwaysInactive = balance -> AccountStatus.INACTIVE;
    assertEquals(AccountStatus.INACTIVE,
            new Account("A005", "Eve", 100.0, alwaysInactive).getStatus());
}
```

- [ ] **Step 2: Run `mvn -q -Dtest=AccountTest test` and verify compilation fails because the enum, interface, exception, and constructor do not exist**

- [ ] **Step 3: Implement minimal enum, policy, exception, and validated account**

```java
public enum AccountStatus { ACTIVE, INACTIVE }

@FunctionalInterface
public interface AccountStatusPolicy {
    AccountStatus determineStatus(double balance);
}

public class BalanceBasedAccountStatusPolicy implements AccountStatusPolicy {
    public AccountStatus determineStatus(double balance) {
        return balance > 0 ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
    }
}
```

`Account` must no longer be `final`; validate `A\\d{3,}`, nonblank owner, and finite nonnegative balance. Its three-argument constructor delegates to the injectable four-argument constructor. `getStatus()` returns `AccountStatus`, and `toString()` prints the enum name.

- [ ] **Step 4: Run `mvn -q -Dtest=AccountTest test` and verify all account tests pass**

- [ ] **Step 5: Commit `feat: standardize account status with policy`**

### Task 2: Customer Inheritance And Factory

**Files:**
- Create: `src/main/java/com/example/account/domain/CustomerType.java`
- Create: `src/main/java/com/example/account/domain/UserRole.java`
- Create: `src/main/java/com/example/account/customer/Customer.java`
- Create: `src/main/java/com/example/account/customer/StandardCustomer.java`
- Create: `src/main/java/com/example/account/customer/PremiumCustomer.java`
- Create: `src/main/java/com/example/account/customer/CorporateCustomer.java`
- Create: `src/main/java/com/example/account/customer/CustomerFactory.java`
- Create: `src/test/java/com/example/account/customer/CustomerTest.java`

- [ ] **Step 1: Write failing polymorphism and credential privacy tests**

```java
@Test void allCustomerTypesShareOneApi() {
    Customer[] customers = {
        CustomerFactory.create(CustomerType.STANDARD, "C001", "Alice", "alice", "secret1".toCharArray(), "A101", 10),
        CustomerFactory.create(CustomerType.PREMIUM, "C002", "Bob", "bob", "secret2".toCharArray(), "A102", 20),
        CustomerFactory.create(CustomerType.CORPORATE, "C003", "Corp", "corp", "secret3".toCharArray(), "A103", 30)
    };
    assertArrayEquals(CustomerType.values(),
            Arrays.stream(customers).map(Customer::getType).toArray(CustomerType[]::new));
}
@Test void displayNeverContainsPassword() {
    Customer customer = CustomerFactory.create(CustomerType.STANDARD,
            "C001", "Alice", "alice", "top-secret".toCharArray(), "A101", 10);
    assertFalse(customer.toString().contains("top-secret"));
}
```

- [ ] **Step 2: Run `mvn -q -Dtest=CustomerTest test` and verify missing customer types cause failure**

- [ ] **Step 3: Implement the abstract base, three subtypes, and switch-based factory**

`Customer` exposes `getCustomerId()`, `getName()`, `getUsername()`, `matchesPassword(char[])`, `getAccount()`, abstract `getType()`, and `UserRole.CUSTOMER`. `matchesPassword` performs constant-time byte comparison and never returns credential data. Store passwords as defensive `char[]` copies and exclude them from `toString()`. Each subtype only returns its enum type. The factory accepts `char[]` and validates all arguments through constructors.

- [ ] **Step 4: Run `mvn -q -Dtest=CustomerTest test` and verify all customer tests pass**

- [ ] **Step 5: Commit `feat: model customer tiers with inheritance`**

### Task 3: Fixed-Capacity Array Repository

**Files:**
- Create: `src/main/java/com/example/account/exception/DuplicateCustomerException.java`
- Create: `src/main/java/com/example/account/exception/CustomerNotFoundException.java`
- Create: `src/main/java/com/example/account/exception/RepositoryCapacityException.java`
- Create: `src/main/java/com/example/account/repository/ArrayCustomerRepository.java`
- Create: `src/test/java/com/example/account/repository/ArrayCustomerRepositoryTest.java`

- [ ] **Step 1: Write failing repository behavior tests**

```java
@Test void savesFindsAndDeletesWithoutACollection() {
    ArrayCustomerRepository repository = new ArrayCustomerRepository(2);
    repository.save(alice());
    repository.save(bob());
    assertEquals("Alice", repository.findByUsername("alice").getName());
    repository.deleteByCustomerId("C001");
    assertEquals(1, repository.size());
    assertEquals("C002", repository.findAll()[0].getCustomerId());
}
@Test void rejectsDuplicatesAndCapacityOverflow() {
    ArrayCustomerRepository repository = new ArrayCustomerRepository(1);
    repository.save(alice());
    assertThrows(DuplicateCustomerException.class, () -> repository.save(alice()));
    assertThrows(RepositoryCapacityException.class, () -> repository.save(bob()));
}
```

- [ ] **Step 2: Run `mvn -q -Dtest=ArrayCustomerRepositoryTest test` and verify the repository is missing**

- [ ] **Step 3: Implement `Customer[] customers`, `int size`, defensive `findAll()` copying, linear lookup, uniqueness checks for ID/username/account number, compaction on delete, and capacity checks**

- [ ] **Step 4: Run `mvn -q -Dtest=ArrayCustomerRepositoryTest test` and verify repository tests pass**

- [ ] **Step 5: Commit `feat: add array-backed customer repository`**

### Task 4: Authentication, Registration, And Authorization

**Files:**
- Create: `src/main/java/com/example/account/exception/AuthenticationException.java`
- Create: `src/main/java/com/example/account/exception/AuthorizationException.java`
- Create: `src/main/java/com/example/account/security/UserSession.java`
- Create: `src/main/java/com/example/account/service/AuthenticationService.java`
- Create: `src/main/java/com/example/account/service/CustomerService.java`
- Create: `src/test/java/com/example/account/service/AuthenticationServiceTest.java`
- Create: `src/test/java/com/example/account/service/CustomerServiceTest.java`

- [ ] **Step 1: Write failing login, registration, logout-state, and role-boundary tests**

```java
@Test void adminAndCustomerCanLoginButWrongPasswordFails() {
    assertEquals(UserRole.ADMIN, auth.login("admin", "Admin123".toCharArray()).role());
    assertEquals(UserRole.CUSTOMER, auth.login("alice", "Secret123".toCharArray()).role());
    assertThrows(AuthenticationException.class,
            () -> auth.login("alice", "wrong".toCharArray()));
}
@Test void registrationAddsCustomerAndDuplicateUsernameFails() {
    UserSession session = auth.register(CustomerType.PREMIUM, "C006", "Faye", "faye",
            "Secret123".toCharArray(), "A106", 50);
    assertEquals(UserRole.CUSTOMER, session.role());
    assertThrows(DuplicateCustomerException.class,
            () -> auth.register(CustomerType.STANDARD, "C007", "Other", "faye",
                    "Password9".toCharArray(), "A107", 0));
}
@Test void customerCannotListOrDeleteOtherCustomers() {
    assertThrows(AuthorizationException.class, () -> service.listAll(customerSession));
    assertThrows(AuthorizationException.class,
            () -> service.deleteCustomer(customerSession, "C002"));
}
```

- [ ] **Step 2: Run the two service test classes and verify missing services cause failure**

- [ ] **Step 3: Implement immutable `UserSession(username, role, customerId)`, a built-in `admin/Admin123` credential, registration through `CustomerFactory`, login using constant-time `MessageDigest.isEqual`, and administrator checks in `CustomerService`**

`CustomerService.getOwnAccount(session)` permits customers to view only the matching record. `addCustomer`, `deleteCustomer`, and `listAll` require `ADMIN`. Neither service retains global login state; logout is implemented by the controller discarding its current `UserSession`.

- [ ] **Step 4: Run `mvn -q -Dtest=AuthenticationServiceTest,CustomerServiceTest test` and verify all service tests pass**

- [ ] **Step 5: Commit `feat: add authentication and role authorization`**

### Task 5: Lambda Search And Audit Filters

**Files:**
- Modify: `src/main/java/com/example/account/repository/ArrayCustomerRepository.java`
- Modify: `src/main/java/com/example/account/service/CustomerService.java`
- Modify: `src/test/java/com/example/account/service/CustomerServiceTest.java`

- [ ] **Step 1: Add failing search and predicate-composition tests**

```java
@Test void adminCanSearchAcrossIdentityFields() {
    assertEquals("C001", service.search(admin, "alice")[0].getCustomerId());
    assertEquals("C002", service.search(admin, "A102")[0].getCustomerId());
}
@Test void lambdasFilterByTypeStatusAndMinimumBalance() {
    Predicate<Customer> premium = c -> c.getType() == CustomerType.PREMIUM;
    Predicate<Customer> funded = c -> c.getAccount().getStatus() == AccountStatus.ACTIVE
            && c.getAccount().getBalance() >= 10_000;
    Customer[] result = service.filter(admin, premium.and(funded));
    assertArrayEquals(new String[] {"C004"},
            Arrays.stream(result).map(Customer::getCustomerId).toArray(String[]::new));
}
```

- [ ] **Step 2: Run `mvn -q -Dtest=CustomerServiceTest test` and verify new methods are missing**

- [ ] **Step 3: Implement case-insensitive search and `filter(UserSession, Predicate<Customer>)` using `Arrays.stream(repository.findAll()).filter(predicate).toArray(Customer[]::new)`; require administrator role**

- [ ] **Step 4: Re-run `CustomerServiceTest` and verify all search/filter tests pass**

- [ ] **Step 5: Commit `feat: add lambda customer search filters`**

### Task 6: Recoverable English Console Interface

**Files:**
- Create: `src/main/java/com/example/account/console/ConsoleIO.java`
- Create: `src/main/java/com/example/account/console/SystemConsoleIO.java`
- Create: `src/main/java/com/example/account/console/ConsoleController.java`
- Create: `src/test/java/com/example/account/console/ScriptedConsoleIO.java`
- Create: `src/test/java/com/example/account/console/ConsoleControllerTest.java`

- [ ] **Step 1: Write failing scripted console tests for invalid choices, login/logout, registration, administrator add/delete/search/filter, customer self-view, and exit**

```java
@Test void invalidChoiceRecoversAndExitStopsCleanly() {
    ScriptedConsoleIO io = new ScriptedConsoleIO("x", "3");
    controller(io).run();
    assertTrue(io.output().contains("Invalid option. Please try again."));
    assertTrue(io.output().contains("Goodbye."));
}
@Test void passwordIsReadThroughMaskedApi() {
    ScriptedConsoleIO io = new ScriptedConsoleIO("1", "admin", "Admin123", "7");
    controller(io).run();
    assertEquals(1, io.passwordReadCount());
    assertFalse(io.output().contains("Admin123"));
}
@Test void failedLoginReturnsToGuestMenu() {
    ScriptedConsoleIO io = new ScriptedConsoleIO("1", "admin", "wrong", "3");
    controller(io).run();
    assertTrue(io.output().contains("Invalid username or password."));
    assertTrue(io.output().contains("1. Login"));
}
```

- [ ] **Step 2: Run `mvn -q -Dtest=ConsoleControllerTest test` and verify console types are missing**

- [ ] **Step 3: Implement `ConsoleIO.readLine`, `readPassword`, and `println`; implement `SystemConsoleIO` with `System.console().readPassword()` and an explicit masking-unavailable warning before stream fallback**

- [ ] **Step 4: Implement guest, admin, and customer menu loops with the exact approved options; catch `ValidationException`, authentication/authorization exceptions, number parsing errors, and repository exceptions and print English messages without terminating**

- [ ] **Step 5: Run `mvn -q -Dtest=ConsoleControllerTest test` and verify every scripted flow passes without passwords in output**

- [ ] **Step 6: Commit `feat: add recoverable English console menus`**

### Task 7: Five Initial Accounts, Compatibility, And Final Verification

**Files:**
- Create: `src/main/java/com/example/account/AccountManagementApplication.java`
- Modify: `src/main/java/com/example/account/AccountDemo.java`
- Modify: `src/test/java/com/example/account/AccountDemoTest.java`
- Create: `src/test/java/com/example/account/AccountManagementApplicationTest.java`
- Modify: `pom.xml`
- Create: `README.md`

- [ ] **Step 1: Replace print-capture tests with failing fixture and composition tests**

```java
@Test void createsExactlyFiveValidInitialCustomers() {
    Customer[] customers = AccountDemo.createCustomers();
    assertEquals(5, customers.length);
    assertArrayEquals(new AccountStatus[] {ACTIVE, INACTIVE, INACTIVE, ACTIVE, ACTIVE},
            Arrays.stream(customers).map(c -> c.getAccount().getStatus()).toArray(AccountStatus[]::new));
}
@Test void initialCustomersCoverAllThreeTiers() {
    assertEquals(Set.of(STANDARD, PREMIUM, CORPORATE),
            Arrays.stream(AccountDemo.createCustomers()).map(Customer::getType).collect(toSet()));
}
```

The third initial account must use zero rather than the legacy negative balance, because the approved validation rejects negative opening balances; it remains `INACTIVE`, preserving visible status behavior.

- [ ] **Step 2: Run `mvn -q -Dtest=AccountDemoTest,AccountManagementApplicationTest test` and verify the new composition API is missing**

- [ ] **Step 3: Remove `final` from `AccountDemo`, implement five initial customer subtypes, build a capacity-100 repository, wire policies/services/controller in `AccountManagementApplication.main`, and make `AccountDemo.main` delegate to it**

- [ ] **Step 4: Configure `maven-jar-plugin` with main class `com.example.account.AccountManagementApplication` and document `mvn test`, `mvn package`, and `java -jar target/account-array-demo-1.0-SNAPSHOT.jar`; include demo credentials `admin/Admin123` and warn users to change them outside this classroom demo**

- [ ] **Step 5: Run `mvn -q test` and verify zero failures/errors**

- [ ] **Step 6: Run `mvn -q package` and verify the executable JAR is created successfully**

- [ ] **Step 7: Run a scripted smoke session against the JAR with guest choice `3`; verify it prints the welcome menu and `Goodbye.` without a stack trace**

- [ ] **Step 8: Search production Java for prohibited remnants with `rg -n '"active"|"inactive"|class AccountDemo final|class Account final|System\\.out\\.println\\(account' src/main/java`; verify no matches**

- [ ] **Step 9: Review all six audit requirements against tests, then commit `feat: complete compliant account management application`**
