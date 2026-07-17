package com.example.account;

public final class Account {
    private final String id;
    private final String owner;
    private final double balance;

    public Account(String id, String owner, double balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
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

    public String getStatus() {
        return balance > 0 ? "active" : "inactive";
    }

    @Override
    public String toString() {
        return "Account{id='" + id + "', owner='" + owner
                + "', balance=" + balance + ", status='" + getStatus() + "'}";
    }
}
