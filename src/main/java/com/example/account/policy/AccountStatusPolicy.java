package com.example.account.policy;

import com.example.account.domain.AccountStatus;

@FunctionalInterface
public interface AccountStatusPolicy {
    AccountStatus determineStatus(double balance);
}
