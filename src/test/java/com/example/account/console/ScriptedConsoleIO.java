package com.example.account.console;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

final class ScriptedConsoleIO implements ConsoleIO {
    private final Deque<String> input = new ArrayDeque<>();
    private final StringBuilder output = new StringBuilder();
    private int passwordReads;

    ScriptedConsoleIO(String... input) {
        this.input.addAll(Arrays.asList(input));
    }

    @Override
    public String readLine(String prompt) {
        output.append(prompt);
        return input.pollFirst();
    }

    @Override
    public char[] readPassword(String prompt) {
        passwordReads++;
        output.append(prompt);
        String value = input.pollFirst();
        return value == null ? null : value.toCharArray();
    }

    @Override
    public void println(String text) {
        output.append(text).append('\n');
    }

    String output() {
        return output.toString();
    }

    int passwordReads() {
        return passwordReads;
    }
}
