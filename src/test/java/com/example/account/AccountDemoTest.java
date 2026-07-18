package com.example.account;

import com.example.account.customer.Customer;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.CustomerType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountDemoTest {
    @Test
    void createsFiveCustomersAcrossEveryTierWithExpectedStatuses() {
        Customer[] customers = AccountDemo.createCustomers();

        assertEquals(5, customers.length);
        for (Customer customer : customers) {
            assertNotNull(customer);
        }
        assertEquals(Set.of(CustomerType.STANDARD, CustomerType.PREMIUM, CustomerType.CORPORATE),
                Arrays.stream(customers).map(Customer::getType).collect(Collectors.toSet()));
        assertArrayEquals(new AccountStatus[] {
                AccountStatus.ACTIVE,
                AccountStatus.INACTIVE,
                AccountStatus.INACTIVE,
                AccountStatus.ACTIVE,
                AccountStatus.ACTIVE
        }, Arrays.stream(customers)
                .map(customer -> customer.getAccount().getStatus())
                .toArray(AccountStatus[]::new));
    }

    @Test
    void compatibilityAccountsAreTheFiveCustomerAccounts() {
        Customer[] customers = AccountDemo.createCustomers();
        Account[] accounts = AccountDemo.createAccounts();

        assertEquals(5, accounts.length);
        for (int index = 0; index < accounts.length; index++) {
            assertNotNull(accounts[index]);
            assertEquals(customers[index].getAccount().getId(), accounts[index].getId());
            assertEquals(customers[index].getAccount().getStatus(), accounts[index].getStatus());
        }
    }

    @Test
    void legacyTypesAndAccountStateAreNotDeclaredFinal() throws NoSuchFieldException {
        assertFalse(Modifier.isFinal(AccountDemo.class.getModifiers()));
        for (String fieldName : new String[] {"id", "owner", "balance", "statusPolicy"}) {
            assertFalse(Modifier.isFinal(Account.class.getDeclaredField(fieldName).getModifiers()));
        }
    }
}
