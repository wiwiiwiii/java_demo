package com.example.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerTest {
    @Test
    void customerTypesShareTheSameBaseClass() {
        Account account = new Account("A101", 25.0);
        Customer[] customers = {
                new StandardCustomer("C1", "Alice", "alice", "one".toCharArray(), account),
                new PremiumCustomer("C2", "Bob", "bob", "two".toCharArray(), account),
                new CorporateCustomer("C3", "Acme", "acme", "three".toCharArray(), account)
        };

        assertInstanceOf(Customer.class, customers[0]);
        assertEquals(CustomerType.STANDARD, customers[0].getType());
        assertEquals(CustomerType.PREMIUM, customers[1].getType());
        assertEquals(CustomerType.CORPORATE, customers[2].getType());
    }

    @Test
    void exposesCustomerDetailsAndAccount() {
        Account account = new Account("A101", 25.0);
        Customer customer = new StandardCustomer("C1", "Alice", "alice", "secret".toCharArray(), account);

        assertEquals("C1", customer.getCustomerId());
        assertEquals("Alice", customer.getName());
        assertEquals("alice", customer.getUsername());
        assertEquals(account, customer.getAccount());
    }

    @Test
    void passwordIsDefensivelyCopiedAndHidden() {
        char[] password = "secret".toCharArray();
        Customer customer = new PremiumCustomer("C1", "Alice", "alice", password, new Account("A101", 1));
        password[0] = 'X';

        assertTrue(customer.matchesPassword("secret".toCharArray()));
        assertFalse(customer.matchesPassword("Secret".toCharArray()));
        assertFalse(customer.matchesPassword("secret-longer".toCharArray()));
        assertFalse(customer.matchesPassword(null));
        assertFalse(customer.toString().contains("secret"));
    }

    @Test
    void invalidCustomerDetailsAreRejected() {
        Account account = new Account("A101", 1);

        assertThrows(AccountException.class,
                () -> new StandardCustomer(" ", "Alice", "alice", "secret".toCharArray(), account));
        assertThrows(AccountException.class,
                () -> new StandardCustomer("C1", null, "alice", "secret".toCharArray(), account));
        assertThrows(AccountException.class,
                () -> new StandardCustomer("C1", "Alice", "", "secret".toCharArray(), account));
        assertThrows(AccountException.class,
                () -> new StandardCustomer("C1", "Alice", "alice", new char[0], account));
        assertThrows(AccountException.class,
                () -> new StandardCustomer("C1", "Alice", "alice", "secret".toCharArray(), null));
    }
}
