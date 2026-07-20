package com.example.account;

public class StandardCustomer extends Customer {
    public StandardCustomer(String customerId, String name, String username, char[] password,
                            Account account) {
        super(customerId, name, username, password, account);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.STANDARD;
    }
}
