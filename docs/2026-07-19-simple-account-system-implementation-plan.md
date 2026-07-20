# Simple Customer Account System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current layered implementation with a single-package classroom application that keeps the English CLI and demonstrates the six audit requirements directly.

**Architecture:** All production classes live in `com.example.account`. `AccountSystem` owns the fixed customer array and simple login state, while `Main` owns console interaction; small domain classes demonstrate Enum, interface, inheritance, exception handling, and Lambda filtering without repository, service, session, or console abstraction layers.

**Tech Stack:** Java 17, Maven, JUnit Jupiter 5.10.2, Java arrays, `Scanner`, `Console`, `Predicate`, Stream API

---

## Final File Map

Production files:

- `Main.java`: English guest, administrator, and customer menus.
- `AccountSystem.java`: five initial customers, array operations, login state, search, and filtering.
- `Account.java`: validated account data and status calculation.
- `AccountStatus.java`: `ACTIVE` / `INACTIVE` enum.
- `AccountStatusRule.java`: status-rule interface.
- `BalanceStatusRule.java`: positive-balance implementation.
- `Customer.java`: abstract customer parent.
- `StandardCustomer.java`, `PremiumCustomer.java`, `CorporateCustomer.java`: customer subclasses.
- `CustomerType.java`: customer-type enum.
- `AccountException.java`: unified application exception.

Tests:

- `AccountTest.java`
- `CustomerTest.java`
- `AccountSystemTest.java`

The refactor deletes all production/test subpackages under `console`, `customer`, `domain`, `exception`, `policy`, `repository`, `security`, and `service`, plus the legacy `AccountDemo` and `AccountManagementApplication` classes.

### Task 1: Flatten The Six Audit Domain Types

**Files:**
- Modify: `src/main/java/com/example/account/Account.java`
- Create: `src/main/java/com/example/account/AccountStatus.java`
- Create: `src/main/java/com/example/account/AccountStatusRule.java`
- Create: `src/main/java/com/example/account/BalanceStatusRule.java`
- Create: `src/main/java/com/example/account/AccountException.java`
- Create: `src/main/java/com/example/account/CustomerType.java`
- Create: `src/main/java/com/example/account/Customer.java`
- Create: `src/main/java/com/example/account/StandardCustomer.java`
- Create: `src/main/java/com/example/account/PremiumCustomer.java`
- Create: `src/main/java/com/example/account/CorporateCustomer.java`
- Rewrite: `src/test/java/com/example/account/AccountTest.java`
- Create: `src/test/java/com/example/account/CustomerTest.java`
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Rewrite focused tests against the simple root-package API**

```java
@Test void balanceRuleReturnsEnumStatus() {
    assertEquals(AccountStatus.ACTIVE, new Account("A001", 1.0).getStatus());
    assertEquals(AccountStatus.INACTIVE, new Account("A002", 0.0).getStatus());
}

@Test void accountAcceptsAnotherStatusRule() {
    AccountStatusRule rule = balance -> AccountStatus.INACTIVE;
    assertEquals(AccountStatus.INACTIVE, new Account("A001", 100.0, rule).getStatus());
}

@Test void customerSubclassesExposeTheirTypes() {
    Customer[] customers = {
        new StandardCustomer("C001", "Alice", "alice", "pass1".toCharArray(), new Account("A001", 10)),
        new PremiumCustomer("C002", "Bob", "bob", "pass2".toCharArray(), new Account("A002", 20)),
        new CorporateCustomer("C003", "Corp", "corp", "pass3".toCharArray(), new Account("A003", 30))
    };
    assertArrayEquals(CustomerType.values(),
            Arrays.stream(customers).map(Customer::getType).toArray(CustomerType[]::new));
}
```

Also assert blank/invalid account values and blank customer fields throw `AccountException`, password match/nonmatch works, and `toString()` excludes passwords.

- [ ] **Step 2: Run `mvn -q -Dtest=AccountTest,CustomerTest test`**

Expected: FAIL because the root-package enums, rule, customer hierarchy, and exception do not exist yet.

- [ ] **Step 3: Implement the minimal root-package domain model**

```java
public enum AccountStatus { ACTIVE, INACTIVE }

@FunctionalInterface
public interface AccountStatusRule {
    AccountStatus getStatus(double balance);
}

public class BalanceStatusRule implements AccountStatusRule {
    public AccountStatus getStatus(double balance) {
        return balance > 0 ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
    }
}

public class AccountException extends RuntimeException {
    public AccountException(String message) { super(message); }
}
```

`Account` has `accountNumber`, `balance`, and `AccountStatusRule`; constructors are `(String, double)` and `(String, double, AccountStatusRule)`. Validate `A\\d{3,}`, finite nonnegative balance, and non-null rule.

`Customer` has customer ID, name, username, defensive `char[]` password, and `Account`; expose direct getters, `matchesPassword(char[])`, abstract `getType()`, and password-free `toString()`. Each subclass constructor delegates to `Customer` and returns one `CustomerType`.

- [ ] **Step 4: Run `mvn -q -Dtest=AccountTest,CustomerTest test`**

Expected: PASS, approximately 10 focused tests.

- [ ] **Step 5: Add CHANGELOG entry and commit**

```bash
git add CHANGELOG.md src/main/java/com/example/account src/test/java/com/example/account/AccountTest.java src/test/java/com/example/account/CustomerTest.java
git commit -m "refactor: flatten audit domain model"
git push origin account-compliance
```

### Task 2: Replace Services And Repository With AccountSystem

**Files:**
- Create: `src/main/java/com/example/account/AccountSystem.java`
- Create: `src/test/java/com/example/account/AccountSystemTest.java`
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Write failing tests for the direct classroom API**

```java
@Test void startsWithFiveCustomersAndSupportsSimpleLogin() {
    AccountSystem system = new AccountSystem();
    assertEquals(5, system.getAllCustomers().length);
    system.login("admin", "Admin123".toCharArray());
    assertTrue(system.isAdminLoggedIn());
    system.logout();
    assertFalse(system.isLoggedIn());
}

@Test void administratorCanAddSearchFilterAndDelete() {
    AccountSystem system = new AccountSystem();
    system.login("admin", "Admin123".toCharArray());
    Customer customer = new PremiumCustomer("C006", "Faye", "faye",
            "pass6".toCharArray(), new Account("A006", 20_000));
    system.addCustomer(customer);
    assertEquals(1, system.searchCustomers("faye").length);
    assertEquals(1, system.filterCustomers(c -> c.getAccount().getBalance() >= 20_000).length);
    system.deleteCustomer("C006");
    assertEquals(5, system.getAllCustomers().length);
}
```

Add tests for customer registration/login/current customer/logout, wrong credentials, duplicate ID/username/account, customer denial of administrator operations, array capacity, defensive `getAllCustomers`, search across four fields, and Lambda filters by type/status/balance.

- [ ] **Step 2: Run `mvn -q -Dtest=AccountSystemTest test`**

Expected: FAIL because `AccountSystem` does not exist.

- [ ] **Step 3: Implement one array-backed `AccountSystem`**

```java
public class AccountSystem {
    private static final int CAPACITY = 20;
    private final Customer[] customers = new Customer[CAPACITY];
    private int customerCount;
    private boolean adminLoggedIn;
    private Customer currentCustomer;

    public Customer[] filterCustomers(Predicate<Customer> condition) {
        requireAdmin();
        if (condition == null) {
            throw new AccountException("Filter is required");
        }
        return Arrays.stream(getAllCustomers()).filter(condition).toArray(Customer[]::new);
    }

    public void logout() {
        adminLoggedIn = false;
        currentCustomer = null;
    }
}
```

Implement the remaining public methods with these exact contracts:

- `login(String, char[])`: recognize the classroom administrator first, otherwise linearly find a customer and set `currentCustomer`; throw `AccountException("Invalid username or password")` on failure.
- `registerCustomer(Customer)`: require logged-out state, validate duplicates, append to the array, and return the customer.
- `addCustomer(Customer)`: call `requireAdmin`, validate duplicates, append, and return the customer.
- `deleteCustomer(String)`: call `requireAdmin`, locate the ID, shift later array elements left, clear the old tail, and decrement `customerCount`.
- `getAllCustomers()`: return `Arrays.copyOf(customers, customerCount)`.
- `searchCustomers(String)`: call `requireAdmin`, reject blank queries, then use a loop and temporary `Customer[]` to match the four documented fields before returning a trimmed copy.

Use private helpers with direct names: `addToArray`, `findByUsername`, `checkDuplicate`, and `requireAdmin`. Initialize five customers in the constructor and generate their passwords as `char[]` without exposing them in display output.

- [ ] **Step 4: Run `mvn -q -Dtest=AccountSystemTest,AccountTest,CustomerTest test`**

Expected: PASS, approximately 20-25 total tests.

- [ ] **Step 5: Add CHANGELOG entry and commit**

```bash
git add CHANGELOG.md src/main/java/com/example/account/AccountSystem.java src/test/java/com/example/account/AccountSystemTest.java
git commit -m "refactor: simplify account system operations"
git push origin account-compliance
```

### Task 3: Replace Console Framework With Main

**Files:**
- Create: `src/main/java/com/example/account/Main.java`
- Modify: `pom.xml`
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Add a small launch test to `AccountSystemTest`**

```java
@Test void mainClassIsConfiguredForTheExecutableJar() throws Exception {
    assertNotNull(Class.forName("com.example.account.Main").getMethod("main", String[].class));
}
```

- [ ] **Step 2: Run `mvn -q -Dtest=AccountSystemTest test`**

Expected: FAIL because `Main` does not exist.

- [ ] **Step 3: Implement direct English console menus in `Main`**

`Main` owns one `Scanner`, one `AccountSystem`, and loop methods named `showGuestMenu`, `showAdminMenu`, and `showCustomerMenu`. Use `readText`, `readPassword`, `readNumber`, `readCustomer`, `printCustomers`, and `printAccount` helpers. `readPassword` calls `System.console().readPassword()` when available and otherwise prints one warning before `scanner.nextLine()` fallback. Catch `AccountException`, `NumberFormatException`, and invalid enum values around each menu operation and continue without stack traces.

Filtering offers customer type, account status, and minimum balance, each implemented with an obvious Lambda passed to `filterCustomers`.

Update `maven-jar-plugin`:

```xml
<mainClass>com.example.account.Main</mainClass>
```

- [ ] **Step 4: Run focused tests, package, and smoke test**

```bash
mvn -q -Dtest=AccountSystemTest,AccountTest,CustomerTest test
mvn -q package
printf '3\n' | java -jar target/account-array-demo-1.0-SNAPSHOT.jar
```

Expected: tests and package pass; smoke output contains `Guest Menu` and `Goodbye.`.

- [ ] **Step 5: Add CHANGELOG entry and commit**

```bash
git add CHANGELOG.md pom.xml src/main/java/com/example/account/Main.java src/test/java/com/example/account/AccountSystemTest.java
git commit -m "refactor: simplify command line interaction"
git push origin account-compliance
```

### Task 4: Delete Old Layers And Rewrite README

**Files:**
- Delete: `src/main/java/com/example/account/{console,customer,domain,exception,policy,repository,security,service}/**`
- Delete: `src/main/java/com/example/account/AccountDemo.java`
- Delete: `src/main/java/com/example/account/AccountManagementApplication.java`
- Delete obsolete tests under the matching subpackages and `AccountDemoTest.java`, `AccountManagementApplicationTest.java`
- Rewrite: `README.md`
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Remove obsolete source and test files after replacement tests are green**

Delete only the old layered classes listed in the approved design. Keep the root-package simple classes and three focused tests.

- [ ] **Step 2: Rewrite README for classroom presentation**

README sections must be:

1. `Customer Account Management System`
2. `Project Structure` with the 11 classes and one-line responsibilities
3. `Run The Project`
4. `Demo Login`
5. `Features`
6. `Six Audit Requirements` mapping each requirement to exact class/method
7. `Suggested Demo Order`
8. `Classroom Scope` explaining in-memory data and simplified authentication

- [ ] **Step 3: Update CHANGELOG with removed layers and simplified tests**

Record deletion of session tokens, repository/service/console layers, specialized exceptions, and security timing mechanisms. State that required classroom behavior remains.

- [ ] **Step 4: Run final verification**

```bash
mvn clean test package
printf '3\n' | java -jar target/account-array-demo-1.0-SNAPSHOT.jar
find src/main/java/com/example/account -mindepth 1 -type d
rg -n 'SessionRegistry|UserSession|AuthenticationService|CustomerService|ArrayCustomerRepository|ConsoleController' src README.md
```

Expected: approximately 20-30 tests pass; JAR exits cleanly; `find` prints no package subdirectories; `rg` prints no obsolete class references except the CHANGELOG history if scanned separately.

- [ ] **Step 5: Commit and push the final simplification**

```bash
git add -A src README.md CHANGELOG.md
git commit -m "refactor: complete classroom account system simplification"
git push origin account-compliance
```
