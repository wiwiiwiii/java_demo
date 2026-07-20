package com.example.account;

public class Account {
    private final String accountNumber;
    private double balance;
    private AccountStatusRule statusRule;

    public Account(String accountNumber, double balance) {
        this(accountNumber, balance, new BalanceStatusRule());
    }

    public Account(String accountNumber, double balance, AccountStatusRule statusRule) {
        if (accountNumber == null || !accountNumber.matches("A\\d{3,}")) {
            throw new AccountException("Account number must be A followed by at least three digits");
        }
        if (!Double.isFinite(balance) || balance < 0) {
            throw new AccountException("Balance must be finite and nonnegative");
        }
        this.accountNumber = accountNumber;
        this.balance = balance;
        if (statusRule == null) {
            throw new AccountException("Status rule must not be null");
        }
        this.statusRule = statusRule;
    }

    public String getAccountNumber() {
        return accountNumber;
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

}
