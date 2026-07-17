package com.example.account.customer;

import com.example.account.domain.CustomerType;

import java.util.Objects;

public final class CustomerFactory {
    private CustomerFactory() {
    }

    public static Customer create(CustomerType type, String customerId, String name,
                                  String username, char[] password, String accountNumber,
                                  double balance) {
        return switch (Objects.requireNonNull(type, "type")) {
            case STANDARD -> new StandardCustomer(
                    customerId, name, username, password, accountNumber, balance);
            case PREMIUM -> new PremiumCustomer(
                    customerId, name, username, password, accountNumber, balance);
            case CORPORATE -> new CorporateCustomer(
                    customerId, name, username, password, accountNumber, balance);
        };
    }
}
