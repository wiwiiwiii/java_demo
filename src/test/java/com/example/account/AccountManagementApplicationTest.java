package com.example.account;

import com.example.account.console.ConsoleController;
import com.example.account.console.ConsoleIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountManagementApplicationTest {
    @Test
    void buildsRunnableControllerWithSeededCustomers() {
        RecordingConsoleIO io = new RecordingConsoleIO(
                "1", "admin", "Admin123", "1", "7");

        ConsoleController controller = AccountManagementApplication.createController(io);
        assertNotNull(controller);
        controller.run();

        assertTrue(io.output().contains("Guest Menu"));
        assertTrue(io.output().contains("Administrator Menu"));
        assertTrue(io.output().contains("C001"));
        assertTrue(io.output().contains("C005"));
        assertTrue(io.output().contains("Goodbye."));
        assertFalse(io.output().contains("Admin123"));
    }

    @Test
    void configuresCapacityForOneHundredCustomers() throws ReflectiveOperationException {
        Field capacity = AccountManagementApplication.class
                .getDeclaredField("REPOSITORY_CAPACITY");
        capacity.setAccessible(true);

        assertTrue(ModifierSupport.isStaticFinal(capacity));
        assertEquals(100, capacity.getInt(null));
    }

    @Test
    void fixturePasswordsAreNotShippedAsStringConstants() throws IOException {
        String resource = "/" + AccountDemo.class.getName().replace('.', '/') + ".class";
        byte[] classBytes;
        try (var input = AccountDemo.class.getResourceAsStream(resource)) {
            assertNotNull(input);
            classBytes = input.readAllBytes();
        }
        String constants = new String(classBytes, StandardCharsets.ISO_8859_1);

        for (String password : new String[] {
                "Customer001", "Customer002", "Customer003", "Customer004", "Customer005"
        }) {
            assertFalse(constants.contains(password));
        }
    }

    private static final class ModifierSupport {
        private ModifierSupport() {
        }

        private static boolean isStaticFinal(Field field) {
            int modifiers = field.getModifiers();
            return java.lang.reflect.Modifier.isStatic(modifiers)
                    && java.lang.reflect.Modifier.isFinal(modifiers);
        }
    }

    private static final class RecordingConsoleIO implements ConsoleIO {
        private final Deque<String> input = new ArrayDeque<>();
        private final StringBuilder output = new StringBuilder();

        private RecordingConsoleIO(String... input) {
            this.input.addAll(Arrays.asList(input));
        }

        @Override
        public String readLine(String prompt) {
            output.append(prompt);
            return input.pollFirst();
        }

        @Override
        public char[] readPassword(String prompt) {
            output.append(prompt);
            String value = input.pollFirst();
            return value == null ? null : value.toCharArray();
        }

        @Override
        public void println(String text) {
            output.append(text).append('\n');
        }

        private String output() {
            return output.toString();
        }
    }
}
