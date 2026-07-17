package com.example.account.customer;

import com.example.account.domain.CustomerType;

public class PremiumCustomer extends Customer {
    public PremiumCustomer(String customerId, String name, String username, char[] password,
                           String accountNumber, double balance) {
        super(customerId, name, username, password, accountNumber, balance);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.PREMIUM;
    }
}
