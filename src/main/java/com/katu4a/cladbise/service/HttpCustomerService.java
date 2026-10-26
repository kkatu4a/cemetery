package com.katu4a.cladbise.service;
import com.katu4a.cladbise.api.*;
import com.katu4a.cladbise.model.Customer;
import java.util.*;
public final class HttpCustomerService implements CustomerService {
    private final ApiClient api;
    public HttpCustomerService(ApiClient api) { this.api = api; }
    public List<Customer> findAll() {
        return api.list("api/customers", ApiModels.Customer.class).stream()
                .filter(c -> c.status() == ApiModels.Status.ACTIVE).map(this::model).toList();
    }
    public Customer create(Customer c) { return save("POST", "api/customers", c); }
    public Customer update(Customer c) { return save("PUT", "api/customers/" + c.id(), c); }
    public void archive(UUID id) { api.call("DELETE", "api/customers/" + id, null, Void.class); }
    private Customer save(String method, String path, Customer c) {
        c = CustomerValidator.validate(c);
        return model(api.call(method, path, new ApiModels.CustomerInput(c.fullName(), c.phone(), c.email()), ApiModels.Customer.class));
    }
    private Customer model(ApiModels.Customer c) { return new Customer(c.id(), c.fullName(), c.phone(), c.email(), c.createdAt()); }
}
