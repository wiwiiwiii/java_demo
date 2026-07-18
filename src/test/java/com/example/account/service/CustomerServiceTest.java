package com.example.account.service;

import com.example.account.Account;
import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.exception.AuthorizationException;
import com.example.account.exception.CustomerNotFoundException;
import com.example.account.exception.ValidationException;
import com.example.account.repository.ArrayCustomerRepository;
import com.example.account.security.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerServiceTest {
    private ArrayCustomerRepository repository;
    private CustomerService service;
    private UserSession admin;
    private UserSession aliceSession;

    @BeforeEach
    void setUp() {
        repository = new ArrayCustomerRepository(5);
        repository.save(customer("C001", "alice", "A101"));
        service = new CustomerService(repository);
        admin = UserSession.admin("admin");
        aliceSession = UserSession.customer("alice", "C001");
    }

    @Test
    void administratorCanListAddAndDeleteCustomers() {
        assertEquals(1, service.listAll(admin).length);

        Customer bob = service.addCustomer(admin, CustomerType.PREMIUM, "C002", "Bob", "bob",
                "secret2".toCharArray(), "A102", 200);
        assertSame(bob, repository.findByCustomerId("C002"));
        assertEquals(2, service.listAll(admin).length);

        service.deleteCustomer(admin, "C002");
        assertThrows(CustomerNotFoundException.class,
                () -> repository.findByCustomerId("C002"));
    }

    @Test
    void customerCanReadOnlyOwnAccount() {
        Account account = service.getOwnAccount(aliceSession);

        assertSame(repository.findByCustomerId("C001").getAccount(), account);
        assertThrows(AuthorizationException.class,
                () -> service.getOwnAccount(UserSession.customer("mallory", "C001")));
    }

    @Test
    void customerCannotUseAdministratorOperations() {
        assertThrows(AuthorizationException.class, () -> service.listAll(aliceSession));
        assertThrows(AuthorizationException.class, () -> service.addCustomer(aliceSession,
                CustomerType.STANDARD, "C002", "Bob", "bob", "secret2".toCharArray(),
                "A102", 10));
        assertThrows(AuthorizationException.class,
                () -> service.deleteCustomer(aliceSession, "C001"));
    }

    @Test
    void missingAndInvalidSessionsAreRejected() {
        assertThrows(AuthorizationException.class, () -> service.listAll(null));
        assertThrows(AuthorizationException.class, () -> service.getOwnAccount(null));
        assertThrows(AuthorizationException.class, () -> service.getOwnAccount(admin));
        assertThrows(ValidationException.class, () -> UserSession.admin(" "));
        assertThrows(ValidationException.class,
                () -> new UserSession("alice", null, "C001"));
        assertThrows(ValidationException.class,
                () -> new UserSession("admin", com.example.account.domain.UserRole.ADMIN, "C001"));
    }

    @Test
    void deletedOrMissingOwnCustomerRecordIsReported() {
        repository.deleteByCustomerId("C001");

        assertThrows(CustomerNotFoundException.class,
                () -> service.getOwnAccount(aliceSession));
        assertThrows(CustomerNotFoundException.class,
                () -> service.getOwnAccount(UserSession.customer("missing", "C999")));
    }

    private static Customer customer(String id, String username, String account) {
        return CustomerFactory.create(CustomerType.STANDARD, id, username, username,
                "secret1".toCharArray(), account, 100);
    }
}
