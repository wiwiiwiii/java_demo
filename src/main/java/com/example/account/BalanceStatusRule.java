package com.example.account;

public class BalanceStatusRule implements AccountStatusRule {
    @Override
    public AccountStatus getStatus(double balance) {
        return balance > 0 ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
    }
}
