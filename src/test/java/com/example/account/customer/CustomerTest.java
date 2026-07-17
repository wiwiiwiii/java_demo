package com.example.account.customer;

import com.example.account.domain.CustomerType;
import com.example.account.domain.UserRole;
import com.example.account.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerTest {
    @Test
    void allCustomerTypesShareOneApiAndPreserveEnumOrder() {
        Customer[] customers = {
                customer(CustomerType.STANDARD, "C001", "Alice", "alice", "secret1", "A101", 10),
                customer(CustomerType.PREMIUM, "C002", "Bob", "bob", "secret2", "A102", 20),
                customer(CustomerType.CORPORATE, "C003", "Corp", "corp", "secret3", "A103", 30)
        };

        assertArrayEquals(CustomerType.values(),
                Arrays.stream(customers).map(Customer::getType).toArray(CustomerType[]::new));
        assertInstanceOf(StandardCustomer.class, customers[0]);
        assertInstanceOf(PremiumCustomer.class, customers[1]);
        assertInstanceOf(CorporateCustomer.class, customers[2]);
        assertEquals("C001", customers[0].getCustomerId());
        assertEquals("Alice", customers[0].getName());
        assertEquals("alice", customers[0].getUsername());
        assertEquals("A101", customers[0].getAccount().getId());
    }

    @Test
    void customerRoleIsAlwaysCustomer() {
        assertEquals(UserRole.CUSTOMER,
                customer(CustomerType.STANDARD, "C001", "Alice", "alice", "secret", "A101", 10)
                        .getRole());
    }

    @Test
    void displayNeverContainsPassword() {
        Customer customer = customer(
                CustomerType.STANDARD, "C001", "Alice", "alice", "top-secret", "A101", 10);

        assertFalse(customer.toString().contains("top-secret"));
        assertTrue(customer.toString().contains("C001"));
    }

    @Test
    void passwordIsDefensivelyCopiedAndCanBeMatched() {
        char[] password = "top-secret".toCharArray();
        Customer customer = CustomerFactory.create(
                CustomerType.STANDARD, "C001", "Alice", "alice", password, "A101", 10);

        password[0] = 'X';

        assertTrue(customer.matchesPassword("top-secret".toCharArray()));
        assertFalse(customer.matchesPassword("Xop-secret".toCharArray()));
        assertFalse(customer.matchesPassword("top-secre".toCharArray()));
        assertFalse(customer.matchesPassword(null));
    }

    @Test
    void blankCustomerFieldsAndPasswordsAreRejected() {
        assertInvalid(null, "Alice", "alice", "secret".toCharArray());
        assertInvalid("  ", "Alice", "alice", "secret".toCharArray());
        assertInvalid("C001", null, "alice", "secret".toCharArray());
        assertInvalid("C001", " ", "alice", "secret".toCharArray());
        assertInvalid("C001", "Alice", null, "secret".toCharArray());
        assertInvalid("C001", "Alice", " ", "secret".toCharArray());
        assertInvalid("C001", "Alice", "alice", null);
        assertInvalid("C001", "Alice", "alice", new char[0]);
        assertInvalid("C001", "Alice", "alice", "   ".toCharArray());
    }

    @Test
    void accountValidationIsAppliedByCustomerConstruction() {
        assertThrows(ValidationException.class, () -> customer(
                CustomerType.STANDARD, "C001", "Alice", "alice", "secret", "invalid", 10));
        assertThrows(ValidationException.class, () -> customer(
                CustomerType.STANDARD, "C001", "Alice", "alice", "secret", "A101", -1));
    }

    @Test
    void factoryRejectsNullCustomerType() {
        assertThrows(NullPointerException.class, () -> customer(
                null, "C001", "Alice", "alice", "secret", "A101", 10));
    }

    private static Customer customer(CustomerType type, String customerId, String name,
                                     String username, String password, String accountNumber,
                                     double balance) {
        return CustomerFactory.create(type, customerId, name, username, password.toCharArray(),
                accountNumber, balance);
    }

    private static void assertInvalid(String customerId, String name, String username,
                                      char[] password) {
        assertThrows(ValidationException.class, () -> CustomerFactory.create(
                CustomerType.STANDARD, customerId, name, username, password, "A101", 10));
    }
}
