package com.example.account;

public final class AccountDemo {
    private AccountDemo() {
    }

    public static Account[] createAccounts() {
        return new Account[] {
                new Account("A001", "张三", 1000.0),
                new Account("A002", "李四", 0.0),
                new Account("A003", "王五", -50.0),
                new Account("A004", "赵六", 2500.5),
                new Account("A005", "钱七", 88.8)
        };
    }

    public static void main(String[] args) {
        Account[] accounts = createAccounts();
        for (Account account : accounts) {
            System.out.println(account);
        }
    }
}
