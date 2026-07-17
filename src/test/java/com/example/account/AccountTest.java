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
    void malformedAndNullAccountNumbersAreRejected() {
        assertThrows(ValidationException.class, () -> new Account(null, "Alice", 1.0));
        assertThrows(ValidationException.class, () -> new Account("B001", "Alice", 1.0));
        assertThrows(ValidationException.class, () -> new Account("A12", "Alice", 1.0));
        assertThrows(ValidationException.class, () -> new Account("A123X", "Alice", 1.0));
    }

    @Test
    void nullAndBlankOwnersAreRejected() {
        assertThrows(ValidationException.class, () -> new Account("A001", null, 1.0));
        assertThrows(ValidationException.class, () -> new Account("A001", "", 1.0));
        assertThrows(ValidationException.class, () -> new Account("A001", "   ", 1.0));
    }

    @Test
    void infiniteBalancesAreRejected() {
        assertThrows(ValidationException.class,
                () -> new Account("A003", "Cara", Double.POSITIVE_INFINITY));
        assertThrows(ValidationException.class,
                () -> new Account("A004", "Dan", Double.NEGATIVE_INFINITY));
    }

    @Test
    void injectedPolicyCanChangeStatusWithoutChangingAccount() {
        double[] forwardedBalance = {Double.NaN};
        AccountStatusPolicy alwaysInactive = balance -> {
            forwardedBalance[0] = balance;
            return AccountStatus.INACTIVE;
        };

        assertEquals(AccountStatus.INACTIVE,
                new Account("A005", "Eve", 100.0, alwaysInactive).getStatus());
        assertEquals(100.0, forwardedBalance[0]);
    }
}
