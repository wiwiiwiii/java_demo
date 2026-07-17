package com.example.account;

import com.example.account.domain.AccountStatus;
import com.example.account.exception.ValidationException;
import com.example.account.policy.AccountStatusPolicy;
import com.example.account.policy.BalanceBasedAccountStatusPolicy;

import java.util.Objects;

public class Account {
    private final String id;
    private final String owner;
    private final double balance;
    private final AccountStatusPolicy statusPolicy;

    public Account(String id, String owner, double balance) {
        this(id, owner, balance, new BalanceBasedAccountStatusPolicy());
    }

    public Account(String id, String owner, double balance, AccountStatusPolicy statusPolicy) {
        if (id == null || !id.matches("A\\d{3,}")) {
            throw new ValidationException("Account number must be A followed by at least three digits");
        }
        if (owner == null || owner.isBlank()) {
            throw new ValidationException("Owner must not be blank");
        }
        if (!Double.isFinite(balance) || balance < 0) {
            throw new ValidationException("Balance must be finite and nonnegative");
        }
        this.id = id;
        this.owner = owner;
        this.balance = balance;
        this.statusPolicy = Objects.requireNonNull(statusPolicy, "statusPolicy");
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return statusPolicy.determineStatus(balance);
    }

    @Override
    public String toString() {
        return "Account{id='" + id + "', owner='" + owner
                + "', balance=" + balance + ", status='" + getStatus() + "'}";
    }
}
