package com.example.account;

public class CorporateCustomer extends Customer {
    public CorporateCustomer(String customerId, String name, String username, char[] password,
                             Account account) {
        super(customerId, name, username, password, account);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.CORPORATE;
    }
}
