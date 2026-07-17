package com.example.account;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDemoTest {
    @Test
    void createsFiveAccounts() {
        Account[] accounts = AccountDemo.createAccounts();

        assertEquals(5, accounts.length);
        for (Account account : accounts) {
            assertNotNull(account);
        }
    }

    @Test
    void mainPrintsFiveAccountsWithStatuses() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            AccountDemo.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String[] lines = output.toString(StandardCharsets.UTF_8).lines().toArray(String[]::new);
        assertEquals(5, lines.length);
        assertTrue(lines[0].contains("status='active'"));
        assertTrue(lines[1].contains("status='inactive'"));
    }
}
