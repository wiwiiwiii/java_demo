package com.example.account;

@FunctionalInterface
public interface AccountStatusRule {
    AccountStatus getStatus(double balance);
}
