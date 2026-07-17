# Account Array Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Java console example that stores five accounts in an array, prints them, derives active/inactive status from balance, and includes automated tests.

**Architecture:** `Account` is an immutable domain object whose status is calculated from its balance. `AccountDemo` owns creation of the five-element array and console output. JUnit 5 tests exercise status boundaries, array contents, and printed output.

**Tech Stack:** Java 17, Maven, JUnit Jupiter 5

---

## File Structure

- `pom.xml`: Maven compiler and JUnit configuration.
- `src/main/java/com/example/account/Account.java`: Account data and derived status.
- `src/main/java/com/example/account/AccountDemo.java`: Five-account array creation and console entry point.
- `src/test/java/com/example/account/AccountTest.java`: Balance-to-status behavior tests.
- `src/test/java/com/example/account/AccountDemoTest.java`: Array and console output tests.

### Task 1: Maven Test Foundation and Account Status

**Files:**
- Create: `pom.xml`
- Create: `src/test/java/com/example/account/AccountTest.java`
- Create: `src/main/java/com/example/account/Account.java`

- [ ] **Step 1: Add Maven and JUnit configuration**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>account-array-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.2</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Write failing account status tests**

```java
package com.example.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountTest {
    @Test
    void positiveBalanceIsActive() {
        Account account = new Account("A001", "张三", 1000.0);
        assertEquals("active", account.getStatus());
    }

    @Test
    void zeroBalanceIsInactive() {
        Account account = new Account("A002", "李四", 0.0);
        assertEquals("inactive", account.getStatus());
    }

    @Test
    void negativeBalanceIsInactive() {
        Account account = new Account("A003", "王五", -10.0);
        assertEquals("inactive", account.getStatus());
    }
}
```

- [ ] **Step 3: Run tests to verify RED**

Run: `mvn -q -Dtest=AccountTest test`

Expected: compilation fails because `Account` does not exist.

- [ ] **Step 4: Implement the minimal Account class**

```java
package com.example.account;

public final class Account {
    private final String id;
    private final String owner;
    private final double balance;

    public Account(String id, String owner, double balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public String getStatus() {
        return balance > 0 ? "active" : "inactive";
    }

    @Override
    public String toString() {
        return "Account{id='" + id + "', owner='" + owner
                + "', balance=" + balance + ", status='" + getStatus() + "'}";
    }
}
```

- [ ] **Step 5: Run the account tests to verify GREEN**

Run: `mvn -q -Dtest=AccountTest test`

Expected: three tests pass with exit code 0.

- [ ] **Step 6: Commit the account behavior**

```bash
git add pom.xml src/main/java/com/example/account/Account.java src/test/java/com/example/account/AccountTest.java
git commit -m "feat: add balance-based account status"
```

### Task 2: Five-Account Array and Printing

**Files:**
- Create: `src/test/java/com/example/account/AccountDemoTest.java`
- Create: `src/main/java/com/example/account/AccountDemo.java`

- [ ] **Step 1: Write failing array and output tests**

```java
package com.example.account;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDemoTest {
    @Test
    void createsFiveAccounts() {
        Account[] accounts = AccountDemo.createAccounts();

        assertEquals(5, accounts.length);
        for (Account account : accounts) {
            assertNotNull(account);
        }
    }

    @Test
    void mainPrintsFiveAccountsWithStatuses() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            AccountDemo.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String[] lines = output.toString(StandardCharsets.UTF_8).lines().toArray(String[]::new);
        assertEquals(5, lines.length);
        assertTrue(lines[0].contains("status='active'"));
        assertTrue(lines[1].contains("status='inactive'"));
    }
}
```

- [ ] **Step 2: Run tests to verify RED**

Run: `mvn -q -Dtest=AccountDemoTest test`

Expected: compilation fails because `AccountDemo` does not exist.

- [ ] **Step 3: Implement the five-account demo**

```java
package com.example.account;

public final class AccountDemo {
    private AccountDemo() {
    }

    public static Account[] createAccounts() {
        return new Account[] {
                new Account("A001", "张三", 1000.0),
                new Account("A002", "李四", 0.0),
                new Account("A003", "王五", -50.0),
                new Account("A004", "赵六", 2500.5),
                new Account("A005", "钱七", 88.8)
        };
    }

    public static void main(String[] args) {
        Account[] accounts = createAccounts();
        for (Account account : accounts) {
            System.out.println(account);
        }
    }
}
```

- [ ] **Step 4: Run all tests to verify GREEN**

Run: `mvn test`

Expected: five tests pass with `BUILD SUCCESS`.

- [ ] **Step 5: Run the console program**

Run: `java -cp target/classes com.example.account.AccountDemo`

Expected: exactly five lines, with A001/A004/A005 active and A002/A003 inactive.

- [ ] **Step 6: Commit the completed demo**

```bash
git add src/main/java/com/example/account/AccountDemo.java src/test/java/com/example/account/AccountDemoTest.java
git commit -m "feat: print five accounts from an array"
```

### Task 3: Final Verification

**Files:**
- Verify: `pom.xml`
- Verify: `src/main/java/com/example/account/Account.java`
- Verify: `src/main/java/com/example/account/AccountDemo.java`
- Verify: `src/test/java/com/example/account/AccountTest.java`
- Verify: `src/test/java/com/example/account/AccountDemoTest.java`

- [ ] **Step 1: Run the complete test suite from a clean build**

Run: `mvn clean test`

Expected: five tests pass with `BUILD SUCCESS` and no failures or errors.

- [ ] **Step 2: Verify console output count and content**

Run: `java -cp target/classes com.example.account.AccountDemo`

Expected output:

```text
Account{id='A001', owner='张三', balance=1000.0, status='active'}
Account{id='A002', owner='李四', balance=0.0, status='inactive'}
Account{id='A003', owner='王五', balance=-50.0, status='inactive'}
Account{id='A004', owner='赵六', balance=2500.5, status='active'}
Account{id='A005', owner='钱七', balance=88.8, status='active'}
```

- [ ] **Step 3: Confirm the worktree is clean**

Run: `git status --short`

Expected: no output after committing the plan and implementation files.
