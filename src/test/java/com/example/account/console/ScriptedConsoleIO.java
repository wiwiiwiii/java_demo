package com.example.account.console;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

final class ScriptedConsoleIO implements ConsoleIO {
    private final Deque<String> input = new ArrayDeque<>();
    private final StringBuilder output = new StringBuilder();
    private final List<char[]> returnedPasswords = new ArrayList<>();
    private Consumer<String> printObserver = text -> { };
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
        if (value == null) {
            return null;
        }
        char[] password = value.toCharArray();
        returnedPasswords.add(password);
        return password;
    }

    @Override
    public void println(String text) {
        output.append(text).append('\n');
        printObserver.accept(text);
    }

    String output() {
        return output.toString();
    }

    int passwordReads() {
        return passwordReads;
    }

    boolean passwordBuffersAreCleared() {
        return returnedPasswords.stream()
                .allMatch(password -> password.length > 0 && allZero(password));
    }

    void onPrint(Consumer<String> observer) {
        printObserver = observer;
    }

    private static boolean allZero(char[] password) {
        for (char character : password) {
            if (character != '\0') {
                return false;
            }
        }
        return true;
    }
}
