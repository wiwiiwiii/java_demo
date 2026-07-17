package com.example.account.customer;

import com.example.account.domain.CustomerType;

public class StandardCustomer extends Customer {
    public StandardCustomer(String customerId, String name, String username, char[] password,
                            String accountNumber, double balance) {
        super(customerId, name, username, password, accountNumber, balance);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.STANDARD;
    }
}
