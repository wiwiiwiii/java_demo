package com.example.account.repository;

import com.example.account.customer.Customer;
import com.example.account.exception.CustomerNotFoundException;
import com.example.account.exception.DuplicateCustomerException;
import com.example.account.exception.RepositoryCapacityException;
import com.example.account.exception.ValidationException;

import java.util.Arrays;

public class ArrayCustomerRepository {
    private final Customer[] customers;
    private int size;

    public ArrayCustomerRepository(int capacity) {
        if (capacity <= 0) {
            throw new ValidationException("Repository capacity must be positive");
        }
        customers = new Customer[capacity];
    }

    public void save(Customer customer) {
        if (customer == null) {
            throw new ValidationException("Customer must not be null");
        }

        for (int index = 0; index < size; index++) {
            Customer existing = customers[index];
            if (existing.getCustomerId().equals(customer.getCustomerId())) {
                throw new DuplicateCustomerException(
                        "Customer ID already exists: " + customer.getCustomerId());
            }
            if (existing.getUsername().equals(customer.getUsername())) {
                throw new DuplicateCustomerException(
                        "Username already exists: " + customer.getUsername());
            }
            if (existing.getAccount().getId().equals(customer.getAccount().getId())) {
                throw new DuplicateCustomerException(
                        "Account number already exists: " + customer.getAccount().getId());
            }
        }

        if (size == customers.length) {
            throw new RepositoryCapacityException("Customer repository is full");
        }
        customers[size++] = customer;
    }

    public Customer findByUsername(String username) {
        for (int index = 0; index < size; index++) {
            if (customers[index].getUsername().equals(username)) {
                return customers[index];
            }
        }
        throw new CustomerNotFoundException("Customer not found for username: " + username);
    }

    public Customer findByCustomerId(String customerId) {
        int index = indexOfCustomerId(customerId);
        if (index >= 0) {
            return customers[index];
        }
        throw new CustomerNotFoundException("Customer not found for ID: " + customerId);
    }

    public Customer[] findAll() {
        return Arrays.copyOf(customers, size);
    }

    public void deleteByCustomerId(String customerId) {
        int index = indexOfCustomerId(customerId);
        if (index < 0) {
            throw new CustomerNotFoundException("Customer not found for ID: " + customerId);
        }

        int elementsToMove = size - index - 1;
        if (elementsToMove > 0) {
            System.arraycopy(customers, index + 1, customers, index, elementsToMove);
        }
        customers[--size] = null;
    }

    public int size() {
        return size;
    }

    private int indexOfCustomerId(String customerId) {
        for (int index = 0; index < size; index++) {
            if (customers[index].getCustomerId().equals(customerId)) {
                return index;
            }
        }
        return -1;
    }
}
