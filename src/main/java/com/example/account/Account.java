package com.example.account;

public class Account {
    private final String accountNumber;
    private String id;
    private double balance;
    private AccountStatusRule statusRule;
    private Object statusPolicy;
    private String owner;

    public Account(String accountNumber, double balance) {
        this(accountNumber, balance, new BalanceStatusRule());
    }

    public Account(String accountNumber, double balance, AccountStatusRule statusRule) {
        this(accountNumber, balance, statusRule, null);
    }

    public Account(String accountNumber, String owner, double balance) {
        this(accountNumber, balance, new BalanceStatusRule(), requireOwner(owner));
    }

    public Account(String accountNumber, String owner, double balance,
                   com.example.account.policy.AccountStatusPolicy statusPolicy) {
        this(accountNumber, balance, adaptLegacyPolicy(statusPolicy), requireOwner(owner));
    }

    private Account(String accountNumber, double balance, AccountStatusRule statusRule, String owner) {
        if (accountNumber == null || !accountNumber.matches("A\\d{3,}")) {
            throw new AccountException("Account number must be A followed by at least three digits");
        }
        if (!Double.isFinite(balance) || balance < 0) {
            throw new AccountException("Balance must be finite and nonnegative");
        }
        this.accountNumber = accountNumber;
        this.id = accountNumber;
        this.balance = balance;
        if (statusRule == null) {
            throw new AccountException("Status rule must not be null");
        }
        this.statusRule = statusRule;
        this.statusPolicy = statusRule;
        this.owner = owner;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getId() {
        return accountNumber;
    }

    public String getOwner() {
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return statusRule.getStatus(balance);
    }

    @Override
    public String toString() {
        return "Account{accountNumber='" + accountNumber + "', balance=" + balance
                + ", status=" + statusRule.getStatus(balance) + "}";
    }

    private static String requireOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new AccountException("Owner must not be blank");
        }
        return owner;
    }

    private static AccountStatusRule adaptLegacyPolicy(
            com.example.account.policy.AccountStatusPolicy statusPolicy) {
        if (statusPolicy == null) {
            throw new AccountException("Status policy must not be null");
        }
        return balance -> statusPolicy.determineStatus(balance)
                == com.example.account.domain.AccountStatus.ACTIVE
                ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
    }
}
