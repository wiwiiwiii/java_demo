package com.example.account.console;

import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.service.AuthenticationService;
import com.example.account.service.CustomerService;
import com.example.account.service.SessionRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleControllerTest {
    @Test
    void invalidGuestChoiceAndFailedLoginReturnToGuestMenu() {
        Fixture fixture = fixture("9", "1", "missing", "wrong-password", "3");

        fixture.controller.run();

        assertTrue(fixture.io.output().contains("Invalid option"));
        assertTrue(fixture.io.output().contains("Invalid username or password"));
        assertTrue(occurrences(fixture.io.output(), "Guest Menu") >= 3);
        assertEquals(1, fixture.io.passwordReads());
        assertFalse(fixture.io.output().contains("wrong-password"));
    }

    @Test
    void administratorCanLoginRecoverFromInvalidChoiceAndLogout() {
        Fixture fixture = fixture("1", "admin", "Admin123", "0", "6", "3");

        fixture.controller.run();

        assertTrue(fixture.io.output().contains("Administrator Menu"));
        assertTrue(fixture.io.output().contains("Invalid option"));
        assertTrue(fixture.io.output().contains("Logged out"));
        assertFalse(fixture.io.output().contains("Admin123"));
    }

    @Test
    void customerSeesOnlyOwnAccountAndCanLogout() {
        Fixture fixture = fixture("1", "alice", "secret1", "8", "1", "3");

        fixture.controller.run();

        assertTrue(fixture.io.output().contains("Customer Menu"));
        assertTrue(fixture.io.output().contains("A101"));
        assertTrue(fixture.io.output().contains("Invalid option"));
        assertFalse(fixture.io.output().contains("List all customers"));
        assertFalse(fixture.io.output().contains("secret1"));
    }

    @Test
    void registrationLogsCustomerInAndReportsValidationErrorsRecoverably() {
        Fixture fixture = fixture(
                "2", "OTHER", "C002", "Bob", "bob", "secret2", "A102", "20",
                "2", "PREMIUM", "C002", "Bob", "bob", "secret2", "A102", "bad",
                "2", "PREMIUM", "C002", "Bob", "bob", "secret2", "A102", "20",
                "1", "3");

        fixture.controller.run();

        assertTrue(fixture.io.output().contains("Invalid customer type"));
        assertTrue(fixture.io.output().contains("Invalid number"));
        assertTrue(fixture.io.output().contains("Registration successful"));
        assertTrue(fixture.io.output().contains("Customer Menu"));
        assertEquals(3, fixture.io.passwordReads());
        assertFalse(fixture.io.output().contains("secret2"));
        assertEquals(2, fixture.repository.size());
    }

    @Test
    void registrationServiceErrorReturnsToGuestMenu() {
        Fixture fixture = fixture("2", "STANDARD", "C001", "Duplicate", "dupe",
                "hidden", "A109", "10", "3");

        fixture.controller.run();

        assertTrue(fixture.io.output().contains("Customer ID already exists"));
        assertTrue(occurrences(fixture.io.output(), "Guest Menu") >= 2);
        assertFalse(fixture.io.output().contains("hidden"));
    }

    @Test
    void administratorCanListSearchAndFilterCustomersWithInputRecovery() {
        Fixture fixture = fixture("1", "admin", "Admin123",
                "1", "2", "alice",
                "3", "9",
                "3", "1", "BAD",
                "3", "1", "STANDARD",
                "3", "2", "BAD",
                "3", "2", "ACTIVE",
                "3", "3", "oops",
                "3", "3", "50",
                "7");

        fixture.controller.run();

        assertTrue(occurrences(fixture.io.output(), "C001") >= 5);
        assertTrue(fixture.io.output().contains("Invalid filter option"));
        assertTrue(fixture.io.output().contains("Invalid customer type"));
        assertTrue(fixture.io.output().contains("Invalid account status"));
        assertTrue(fixture.io.output().contains("Invalid number"));
    }

    @Test
    void administratorCanAddAndDeleteOnlyAfterConfirmation() {
        Fixture fixture = fixture("1", "admin", "Admin123",
                "4", "CORPORATE", "C002", "Bob Corp", "bob", "top-secret", "A102", "500",
                "5", "C002", "n",
                "5", "C002", "y",
                "7");

        fixture.controller.run();

        assertTrue(fixture.io.output().contains("Customer added"));
        assertTrue(fixture.io.output().contains("Deletion cancelled"));
        assertTrue(fixture.io.output().contains("Customer deleted"));
        assertFalse(fixture.io.output().contains("top-secret"));
        assertThrows(RuntimeException.class, () -> fixture.repository.findByCustomerId("C002"));
    }

    @Test
    void addValidationErrorReturnsToAdministratorMenu() {
        Fixture fixture = fixture("1", "admin", "Admin123",
                "4", "NOPE", "C002", "Bob", "bob", "hidden", "A102", "10",
                "4", "STANDARD", "C002", "Bob", "bob", "hidden", "A102", "NaN",
                "7");

        fixture.controller.run();

        assertTrue(fixture.io.output().contains("Invalid customer type"));
        assertTrue(fixture.io.output().contains("Balance must be finite"));
        assertTrue(occurrences(fixture.io.output(), "Administrator Menu") >= 3);
    }

    @Test
    void exitLogsOutActiveSessionsAndEofExitsCleanly() {
        Fixture admin = fixture("1", "admin", "Admin123", "7");
        admin.controller.run();
        Fixture customer = fixture("1", "alice", "secret1", "2");
        customer.controller.run();
        Fixture eof = fixture();

        eof.controller.run();

        assertTrue(admin.io.output().contains("Goodbye"));
        assertTrue(customer.io.output().contains("Goodbye"));
        assertTrue(eof.io.output().contains("Input closed"));
    }

    private static Fixture fixture(String... input) {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(10);
        repository.save(CustomerFactory.create(CustomerType.STANDARD, "C001", "Alice",
                "alice", "secret1".toCharArray(), "A101", 100));
        SessionRegistry sessions = new SessionRegistry();
        ScriptedConsoleIO io = new ScriptedConsoleIO(input);
        ConsoleController controller = new ConsoleController(io,
                new AuthenticationService(repository, sessions),
                new CustomerService(repository, sessions));
        return new Fixture(repository, io, controller);
    }

    private static int occurrences(String text, String value) {
        return (text.length() - text.replace(value, "").length()) / value.length();
    }

    private record Fixture(ArrayCustomerRepository repository, ScriptedConsoleIO io,
                           ConsoleController controller) {
    }
}
