package com.example.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {
    @Test
    void positiveBalanceIsActive() {
        Account account = new Account("A001", 10.0);

        assertEquals("A001", account.getAccountNumber());
        assertEquals(10.0, account.getBalance());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void zeroBalanceIsInactive() {
        assertEquals(AccountStatus.INACTIVE, new Account("A999", 0.0).getStatus());
    }

    @Test
    void customRuleReceivesTheBalance() {
        AccountStatusRule rule = balance -> balance >= 100 ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;

        assertEquals(AccountStatus.INACTIVE, new Account("A123", 99.0, rule).getStatus());
    }

    @Test
    void invalidAccountNumbersAreRejected() {
        assertThrows(AccountException.class, () -> new Account(null, 1.0));
        assertThrows(AccountException.class, () -> new Account("B123", 1.0));
        assertThrows(AccountException.class, () -> new Account("A12", 1.0));
        assertThrows(AccountException.class, () -> new Account("A123x", 1.0));
    }

    @Test
    void invalidBalancesAndRulesAreRejected() {
        assertThrows(AccountException.class, () -> new Account("A123", -0.01));
        assertThrows(AccountException.class, () -> new Account("A123", Double.NaN));
        assertThrows(AccountException.class, () -> new Account("A123", Double.POSITIVE_INFINITY));
        assertThrows(AccountException.class, () -> new Account("A123", 1.0, null));
    }

    @Test
    void legacyConstructorRejectsNullPolicyImmediately() {
        assertThrows(AccountException.class,
                () -> new Account("A123", "Alice", 1.0,
                        (com.example.account.policy.AccountStatusPolicy) null));
    }
}
