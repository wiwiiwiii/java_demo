package com.example.account.console;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class SystemConsoleIO implements ConsoleIO {
    private static final String MASKING_WARNING =
            "Warning: password masking is unavailable. Use a system terminal for masked input.";

    private final Console console;
    private final BufferedReader input;
    private final PrintStream output;
    private boolean warningPrinted;

    public SystemConsoleIO() {
        this(System.console(), new BufferedReader(new InputStreamReader(
                System.in, StandardCharsets.UTF_8)), System.out);
    }

    SystemConsoleIO(Console console, BufferedReader input, PrintStream output) {
        this.console = console;
        this.input = input;
        this.output = output;
    }

    @Override
    public String readLine(String prompt) {
        if (console != null) {
            return console.readLine("%s", prompt);
        }
        output.print(prompt);
        output.flush();
        try {
            return input.readLine();
        } catch (IOException exception) {
            return null;
        }
    }

    @Override
    public char[] readPassword(String prompt) {
        if (console != null) {
            return console.readPassword("%s", prompt);
        }
        if (!warningPrinted) {
            output.println(MASKING_WARNING);
            warningPrinted = true;
        }
        String password = readLine(prompt);
        return password == null ? null : password.toCharArray();
    }

    @Override
    public void println(String text) {
        output.println(text);
    }
}
