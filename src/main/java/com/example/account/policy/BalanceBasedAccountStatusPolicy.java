package com.example.account.policy;

import com.example.account.domain.AccountStatus;

public class BalanceBasedAccountStatusPolicy implements AccountStatusPolicy {
    @Override
    public AccountStatus determineStatus(double balance) {
        return balance > 0 ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
    }
}
