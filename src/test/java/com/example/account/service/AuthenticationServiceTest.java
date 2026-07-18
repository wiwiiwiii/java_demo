package com.example.account.service;

import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.domain.UserRole;
import com.example.account.exception.AuthenticationException;
import com.example.account.exception.DuplicateCustomerException;
import com.example.account.exception.ValidationException;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.security.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {
    private ArrayCustomerRepository repository;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        repository = new ArrayCustomerRepository(5);
        repository.save(customer("C001", "alice", "secret1", "A101"));
        service = new AuthenticationService(repository);
    }

    @Test
    void logsInBuiltInAdministrator() {
        UserSession session = service.login("admin", "Admin123".toCharArray());

        assertEquals("admin", session.username());
        assertEquals(UserRole.ADMIN, session.role());
        assertNull(session.customerId());
    }

    @Test
    void logsInCustomerWithRepositoryCredentials() {
        UserSession session = service.login("alice", "secret1".toCharArray());

        assertEquals("alice", session.username());
        assertEquals(UserRole.CUSTOMER, session.role());
        assertEquals("C001", session.customerId());
    }

    @Test
    void wrongUsernameAndPasswordAreIndistinguishable() {
        AuthenticationException wrongUsername = assertThrows(AuthenticationException.class,
                () -> service.login("missing", "secret1".toCharArray()));
        AuthenticationException wrongPassword = assertThrows(AuthenticationException.class,
                () -> service.login("alice", "wrong".toCharArray()));

        assertEquals("Invalid username or password", wrongUsername.getMessage());
        assertEquals(wrongUsername.getMessage(), wrongPassword.getMessage());
    }

    @Test
    void nullAndBlankCredentialsFailSafely() {
        assertThrows(AuthenticationException.class, () -> service.login(null, null));
        assertThrows(AuthenticationException.class,
                () -> service.login("  ", "secret1".toCharArray()));
        assertThrows(AuthenticationException.class, () -> service.login("alice", null));
        assertThrows(AuthenticationException.class,
                () -> service.login("alice", "   ".toCharArray()));
    }

    @Test
    void registersCustomerAndReturnsCustomerSession() {
        char[] password = "private2".toCharArray();

        UserSession session = service.register(CustomerType.PREMIUM, "C002", "Bob", "bob",
                password, "A102", 200);
        password[0] = 'X';

        Customer saved = repository.findByCustomerId("C002");
        assertEquals(UserRole.CUSTOMER, session.role());
        assertEquals("C002", session.customerId());
        assertTrue(saved.matchesPassword("private2".toCharArray()));
        assertFalse(saved.matchesPassword(password));
        assertFalse(session.toString().contains("private2"));
    }

    @Test
    void registrationPropagatesDuplicateAndValidationFailures() {
        assertThrows(DuplicateCustomerException.class, () -> service.register(
                CustomerType.STANDARD, "C001", "Other", "other", "secret".toCharArray(),
                "A102", 10));
        assertThrows(ValidationException.class, () -> service.register(
                CustomerType.STANDARD, "C002", "Bob", "  ", "secret".toCharArray(),
                "A102", 10));
        assertThrows(ValidationException.class, () -> service.register(
                null, "C002", "Bob", "bob", "secret".toCharArray(), "A102", 10));
    }

    @Test
    void registrationRejectsReservedAdministratorUsernameCaseInsensitively() {
        assertThrows(ValidationException.class, () -> service.register(
                CustomerType.STANDARD, "C002", "Imposter", "ADMIN",
                "secret".toCharArray(), "A102", 10));
        assertEquals(1, repository.size());
    }

    @Test
    void logoutInvalidatesIssuedSession() {
        UserSession session = service.login("alice", "secret1".toCharArray());
        CustomerService customerService = new CustomerService(repository, service);

        service.logout(session);

        assertThrows(com.example.account.exception.AuthorizationException.class,
                () -> customerService.getOwnAccount(session));
    }

    @Test
    void separatelyIssuedSessionsHaveDistinctTokensAndLogoutIsSelective() {
        UserSession first = service.login("alice", "secret1".toCharArray());
        UserSession second = service.login("alice", "secret1".toCharArray());
        CustomerService customerService = new CustomerService(repository, service);

        assertFalse(first.token().equals(second.token()));
        service.logout(first);

        assertThrows(com.example.account.exception.AuthorizationException.class,
                () -> customerService.getOwnAccount(first));
        assertEquals("A101", customerService.getOwnAccount(second).getId());
    }

    private static Customer customer(String id, String username, String password, String account) {
        return CustomerFactory.create(CustomerType.STANDARD, id, username, username,
                password.toCharArray(), account, 100);
    }
}
