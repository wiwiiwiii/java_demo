package com.example.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountTest {
    @Test
    void positiveBalanceIsActive() {
        Account account = new Account("A001", "张三", 1000.0);

        assertEquals("active", account.getStatus());
    }

    @Test
    void zeroBalanceIsInactive() {
        Account account = new Account("A002", "李四", 0.0);

        assertEquals("inactive", account.getStatus());
    }

    @Test
    void negativeBalanceIsInactive() {
        Account account = new Account("A003", "王五", -10.0);

        assertEquals("inactive", account.getStatus());
    }
}
