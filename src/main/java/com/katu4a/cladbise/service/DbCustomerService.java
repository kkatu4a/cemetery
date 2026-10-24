package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Customer;
import com.katu4a.cladbise.util.BackendProvider;

import java.time.LocalDateTime;
import java.util.List;

public class DbCustomerService implements CustomerService {
    private final StubCustomerService stubFallback = new StubCustomerService();

    private boolean isDbAvailable() {
        try {
            return BackendProvider.getDataBase() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Customer> findAll() {
        if (!isDbAvailable()) {
            return stubFallback.findAll();
        }
        try {
            return BackendProvider.getCustomerRepository().getAll().stream()
                    .map(c -> new Customer(c.id(), c.fullName(), c.phone(), c.email(), c.createdAt()))
                    .toList();
        } catch (Exception e) {
            return stubFallback.findAll();
        }
    }

    @Override
    public Customer create(Customer customer) {
        if (!isDbAvailable()) {
            return stubFallback.create(customer);
        }
        try {
            com.arnor4eck.model.Customer dbModel = new com.arnor4eck.model.Customer(
                    customer.id(),
                    customer.fullName(),
                    customer.phone(),
                    customer.email(),
                    customer.createdAt() != null ? customer.createdAt() : LocalDateTime.now()
            );
            BackendProvider.getCustomerRepository().save(dbModel);
            return findAll().stream()
                    .filter(c -> c.phone().equals(customer.phone()))
                    .reduce((first, second) -> second)
                    .orElse(customer);
        } catch (Exception e) {
            return stubFallback.create(customer);
        }
    }

    @Override
    public Customer update(Customer customer) {
        if (!isDbAvailable()) {
            return stubFallback.update(customer);
        }
        try {
            com.arnor4eck.model.Customer dbModel = new com.arnor4eck.model.Customer(
                    customer.id(),
                    customer.fullName(),
                    customer.phone(),
                    customer.email(),
                    customer.createdAt() != null ? customer.createdAt() : LocalDateTime.now()
            );
            BackendProvider.getCustomerRepository().update(dbModel);
            return customer;
        } catch (Exception e) {
            return stubFallback.update(customer);
        }
    }

    @Override
    public void delete(int id) {
        if (!isDbAvailable()) {
            stubFallback.delete(id);
            return;
        }
        try {
            BackendProvider.getCustomerRepository().delete(id);
        } catch (Exception e) {
            stubFallback.delete(id);
        }
    }
}
