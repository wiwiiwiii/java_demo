package com.example.account;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class AccountSystemTest {
    @Test
    void mainClassProvidesExecutableEntryPoint() throws Exception {
        assertNotNull(Class.forName("com.example.account.Main")
                .getMethod("main", String[].class));
    }

    @Test
    void startsWithFiveCustomersAndSupportsSimpleLogin() {
        AccountSystem system = new AccountSystem();
        assertEquals(5, system.getAllCustomers().length);
        system.login("admin", "Admin123".toCharArray());
        assertTrue(system.isAdminLoggedIn());
        assertTrue(system.isLoggedIn());
        system.logout();
        assertFalse(system.isLoggedIn());
    }

    @Test
    void seededCustomersIncludeEveryCustomerType() {
        CustomerType[] types = java.util.Arrays.stream(new AccountSystem().getAllCustomers())
                .map(Customer::getType)
                .distinct()
                .sorted()
                .toArray(CustomerType[]::new);

        assertArrayEquals(CustomerType.values(), types);
    }

    @Test
    void customerCanLoginAndLogout() {
        AccountSystem system = new AccountSystem();
        Customer customer = system.getAllCustomers()[0];
        system.login(customer.getUsername(), "Password1".toCharArray());
        assertSame(customer, system.getCurrentCustomer());
        assertFalse(system.isAdminLoggedIn());
        system.logout();
        assertNull(system.getCurrentCustomer());
    }

    @Test
    void wrongCredentialsAreRejectedWithoutChangingSession() {
        AccountSystem system = new AccountSystem();
        AccountException error = assertThrows(AccountException.class,
                () -> system.login("nobody", "wrong".toCharArray()));
        assertEquals("Invalid username or password", error.getMessage());
        assertFalse(system.isLoggedIn());
    }

    @Test
    void loggedOutCustomerCanRegisterAndThenLogin() {
        AccountSystem system = new AccountSystem();
        Customer customer = customer("C006", "Faye", "faye", "A006", 20_000);
        assertSame(customer, system.registerCustomer(customer));
        system.login("faye", "pass6".toCharArray());
        assertSame(customer, system.getCurrentCustomer());
    }

    @Test
    void registrationRequiresLoggedOutState() {
        AccountSystem system = adminSystem();
        assertThrows(AccountException.class,
                () -> system.registerCustomer(customer("C006", "Faye", "faye", "A006", 1)));
    }

    @Test
    void duplicateIdUsernameAndAccountAreRejected() {
        AccountSystem system = new AccountSystem();
        Customer existing = system.getAllCustomers()[0];
        assertThrows(AccountException.class, () -> system.registerCustomer(
                customer(existing.getCustomerId(), "New", "new-user", "A900", 1)));
        assertThrows(AccountException.class, () -> system.registerCustomer(
                customer("C900", "New", existing.getUsername(), "A901", 1)));
        assertThrows(AccountException.class, () -> system.registerCustomer(
                customer("C901", "New", "new-user", existing.getAccount().getAccountNumber(), 1)));
    }

    @Test
    void administratorCanAddSearchFilterAndDelete() {
        AccountSystem system = adminSystem();
        Customer customer = customer("C006", "Faye", "faye", "A006", 20_000);
        assertSame(customer, system.addCustomer(customer));
        assertEquals(1, system.searchCustomers("faye").length);
        assertEquals(1, system.searchCustomers("C006").length);
        assertEquals(1, system.searchCustomers("FAYE").length);
        assertEquals(1, system.searchCustomers("A006").length);
        assertEquals(3, system.filterCustomers(c -> c instanceof PremiumCustomer).length);
        assertEquals(1, system.filterCustomers(c -> c.getAccount().getBalance() >= 20_000).length);
        system.deleteCustomer("C006");
        assertEquals(5, system.getAllCustomers().length);
    }

    @Test
    void deletingMiddleCustomerCompactsArrayAndPreservesOrder() {
        AccountSystem system = adminSystem();
        Customer[] before = system.getAllCustomers();

        system.deleteCustomer(before[2].getCustomerId());

        Customer[] after = system.getAllCustomers();
        assertEquals(4, after.length);
        assertArrayEquals(
                new Customer[]{before[0], before[1], before[3], before[4]},
                after);
    }

    @Test
    void deletingMissingCustomerThrowsAccountException() {
        AccountSystem system = adminSystem();

        assertThrows(AccountException.class, () -> system.deleteCustomer("C999"));
    }

    @Test
    void administratorOperationsRejectCustomersAndGuests() {
        AccountSystem system = new AccountSystem();
        assertThrows(AccountException.class, () -> system.addCustomer(
                customer("C006", "Faye", "faye", "A006", 1)));
        Customer first = system.getAllCustomers()[0];
        system.login(first.getUsername(), "Password1".toCharArray());
        assertThrows(AccountException.class, () -> system.searchCustomers("C"));
        assertThrows(AccountException.class, () -> system.filterCustomers(c -> true));
        assertThrows(AccountException.class, () -> system.deleteCustomer(first.getCustomerId()));
    }

    @Test
    void blankSearchAndNullFilterAreRejected() {
        AccountSystem system = adminSystem();
        assertThrows(AccountException.class, () -> system.searchCustomers("  "));
        assertThrows(AccountException.class, () -> system.filterCustomers(null));
    }

    @Test
    void getAllCustomersReturnsDefensiveCopy() {
        AccountSystem system = new AccountSystem();
        Customer[] copy = system.getAllCustomers();
        copy[0] = null;
        assertNotNull(system.getAllCustomers()[0]);
    }

    @Test
    void fixedArrayRejectsTwentyFirstCustomer() {
        AccountSystem system = adminSystem();
        for (int i = 6; i <= 20; i++) {
            system.addCustomer(customer("C" + i, "Name" + i, "user" + i, "A" + (100 + i), i));
        }
        assertThrows(AccountException.class, () -> system.addCustomer(
                customer("C21", "Name21", "user21", "A121", 21)));
    }

    @Test
    void implementationUsesOneFixedCustomerArray() {
        Field[] arrays = java.util.Arrays.stream(AccountSystem.class.getDeclaredFields())
                .filter(field -> field.getType() == Customer[].class).toArray(Field[]::new);
        assertEquals(1, arrays.length);
        assertTrue(Modifier.isFinal(arrays[0].getModifiers()));
    }

    private static AccountSystem adminSystem() {
        AccountSystem system = new AccountSystem();
        system.login("admin", "Admin123".toCharArray());
        return system;
    }

    private static Customer customer(String id, String name, String username,
                                     String accountNumber, double balance) {
        return new PremiumCustomer(id, name, username, "pass6".toCharArray(),
                new Account(accountNumber, balance));
    }
}
