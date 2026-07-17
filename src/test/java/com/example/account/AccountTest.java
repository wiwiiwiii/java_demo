package com.example.account;

import com.example.account.domain.AccountStatus;
import com.example.account.exception.ValidationException;
import com.example.account.policy.AccountStatusPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {
    @Test
    void positiveBalanceIsActive() {
        assertEquals(AccountStatus.ACTIVE, new Account("A001", "Alice", 1.0).getStatus());
    }

    @Test
    void zeroBalanceIsInactive() {
        assertEquals(AccountStatus.INACTIVE, new Account("A002", "Bob", 0.0).getStatus());
    }

    @Test
    void negativeAndNonFiniteBalancesAreRejected() {
        assertThrows(ValidationException.class, () -> new Account("A003", "Cara", -0.01));
        assertThrows(ValidationException.class, () -> new Account("A004", "Dan", Double.NaN));
    }

    @Test
    void injectedPolicyCanChangeStatusWithoutChangingAccount() {
        AccountStatusPolicy alwaysInactive = balance -> AccountStatus.INACTIVE;

        assertEquals(AccountStatus.INACTIVE,
                new Account("A005", "Eve", 100.0, alwaysInactive).getStatus());
    }
}
