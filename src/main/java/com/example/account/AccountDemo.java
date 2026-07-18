package com.example.account;

import com.example.account.customer.Customer;
import com.example.account.customer.CustomerFactory;
import com.example.account.domain.CustomerType;

import java.util.Arrays;

public class AccountDemo {
    private AccountDemo() {
    }

    public static Customer[] createCustomers() {
        return new Customer[] {
                customer(CustomerType.STANDARD, "C001", "张三", "zhangsan",
                        1, "A001", 1000.0),
                customer(CustomerType.PREMIUM, "C002", "李四", "lisi",
                        2, "A002", 0.0),
                customer(CustomerType.CORPORATE, "C003", "王五", "wangwu",
                        3, "A003", 0.0),
                customer(CustomerType.STANDARD, "C004", "赵六", "zhaoliu",
                        4, "A004", 2500.5),
                customer(CustomerType.PREMIUM, "C005", "钱七", "qianqi",
                        5, "A005", 88.8)
        };
    }

    public static Account[] createAccounts() {
        return Arrays.stream(createCustomers())
                .map(Customer::getAccount)
                .toArray(Account[]::new);
    }

    public static void main(String[] args) {
        AccountManagementApplication.main(args);
    }

    private static Customer customer(CustomerType type, String customerId, String name,
                                     String username, int passwordSuffix, String accountNumber,
                                     double balance) {
        char[] password = fixturePassword(passwordSuffix);
        try {
            return CustomerFactory.create(type, customerId, name, username,
                    password, accountNumber, balance);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static char[] fixturePassword(int suffix) {
        return new char[] {
                'C', 'u', 's', 't', 'o', 'm', 'e', 'r', '0', '0', (char) ('0' + suffix)
        };
    }
}
