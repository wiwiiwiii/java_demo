package com.example.account;

public class PremiumCustomer extends Customer {
    public PremiumCustomer(String customerId, String name, String username, char[] password,
                           Account account) {
        super(customerId, name, username, password, account);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.PREMIUM;
    }
}
