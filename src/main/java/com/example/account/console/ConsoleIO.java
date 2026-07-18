package com.example.account.console;

public interface ConsoleIO {
    String readLine(String prompt);

    char[] readPassword(String prompt);

    void println(String text);
}
