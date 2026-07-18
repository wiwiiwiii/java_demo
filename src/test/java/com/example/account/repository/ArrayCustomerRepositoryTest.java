package com.example.account.repository;

import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;
import com.example.account.exception.CustomerNotFoundException;
import com.example.account.exception.DuplicateCustomerException;
import com.example.account.exception.RepositoryCapacityException;
import com.example.account.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArrayCustomerRepositoryTest {
    @Test
    void savesAndFindsCustomersByUsernameAndCustomerId() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(2);
        Customer alice = customer("C001", "alice", "A101");

        repository.save(alice);

        assertEquals(1, repository.size());
        assertSame(alice, repository.findByUsername("alice"));
        assertSame(alice, repository.findByCustomerId("C001"));
    }

    @Test
    void deleteCompactsRemainingCustomersAndClearsLogicalTail() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(3);
        Customer alice = customer("C001", "alice", "A101");
        Customer bob = customer("C002", "bob", "A102");
        Customer carol = customer("C003", "carol", "A103");
        repository.save(alice);
        repository.save(bob);
        repository.save(carol);

        repository.deleteByCustomerId("C002");

        assertEquals(2, repository.size());
        Customer[] remaining = repository.findAll();
        assertSame(alice, remaining[0]);
        assertSame(carol, remaining[1]);
        assertEquals(2, remaining.length);
        assertThrows(CustomerNotFoundException.class,
                () -> repository.findByCustomerId("C002"));
    }

    @Test
    void findAllReturnsDefensiveCopiesContainingOnlyLogicalEntries() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(3);
        Customer alice = customer("C001", "alice", "A101");
        repository.save(alice);

        Customer[] first = repository.findAll();
        first[0] = null;
        Customer[] second = repository.findAll();

        assertNotSame(first, second);
        assertEquals(1, second.length);
        assertSame(alice, second[0]);
    }

    @Test
    void rejectsDuplicateCustomerIds() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(2);
        repository.save(customer("C001", "alice", "A101"));

        assertThrows(DuplicateCustomerException.class,
                () -> repository.save(customer("C001", "bob", "A102")));
    }

    @Test
    void rejectsDuplicateUsernames() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(2);
        repository.save(customer("C001", "alice", "A101"));

        assertThrows(DuplicateCustomerException.class,
                () -> repository.save(customer("C002", "alice", "A102")));
    }

    @Test
    void rejectsDuplicateAccountNumbers() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(2);
        repository.save(customer("C001", "alice", "A101"));

        assertThrows(DuplicateCustomerException.class,
                () -> repository.save(customer("C002", "bob", "A101")));
    }

    @Test
    void rejectsSaveWhenCapacityIsExhausted() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(1);
        repository.save(customer("C001", "alice", "A101"));

        assertThrows(RepositoryCapacityException.class,
                () -> repository.save(customer("C002", "bob", "A102")));
    }

    @Test
    void duplicateDetectionTakesPrecedenceWhenRepositoryIsFull() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(1);
        repository.save(customer("C001", "alice", "A101"));

        assertThrows(DuplicateCustomerException.class,
                () -> repository.save(customer("C001", "alice", "A101")));
    }

    @Test
    void missingLookupsAndDeletesUseCustomerNotFoundException() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(1);

        assertThrows(CustomerNotFoundException.class,
                () -> repository.findByUsername("missing"));
        assertThrows(CustomerNotFoundException.class,
                () -> repository.findByCustomerId("missing"));
        assertThrows(CustomerNotFoundException.class,
                () -> repository.deleteByCustomerId("missing"));
    }

    @Test
    void rejectsNullCustomers() {
        ArrayCustomerRepository repository = new ArrayCustomerRepository(1);

        assertThrows(ValidationException.class, () -> repository.save(null));
        assertEquals(0, repository.size());
        assertEquals(0, repository.findAll().length);
    }

    @Test
    void rejectsNonPositiveCapacities() {
        assertThrows(ValidationException.class, () -> new ArrayCustomerRepository(0));
        assertThrows(ValidationException.class, () -> new ArrayCustomerRepository(-1));
    }

    private static Customer customer(String customerId, String username, String accountNumber) {
        return CustomerFactory.create(CustomerType.STANDARD, customerId, username, username,
                "secret".toCharArray(), accountNumber, 100);
    }
}
