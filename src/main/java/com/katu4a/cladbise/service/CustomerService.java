package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Customer;

import java.util.List;

public interface CustomerService {

    List<Customer> findAll();

    Customer create(Customer customer);

    Customer update(Customer customer);

    void delete(int id);
}