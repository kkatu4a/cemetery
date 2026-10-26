package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<Customer> findAll();

    Customer create(Customer customer);

    Customer update(Customer customer);

    void delete(UUID id);
}