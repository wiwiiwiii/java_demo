package com.example.account.exception;

public class DuplicateCustomerException extends ValidationException {
    public DuplicateCustomerException(String message) {
        super(message);
    }
}
