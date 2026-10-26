package com.katu4a.cladbise.service;
import com.katu4a.cladbise.api.*;
import com.katu4a.cladbise.model.Employee;
import java.util.*;
public final class HttpEmployeeService implements EmployeeService {
    private final ApiClient api;
    public HttpEmployeeService(ApiClient api) { this.api = api; }
    public List<Employee> findAll() {
        return api.list("api/employees", ApiModels.Employee.class).stream()
                .filter(e -> e.status() == ApiModels.Status.ACTIVE).map(this::model).toList();
    }
    public Employee create(Employee e, String password) {
        if (password == null || password.isBlank()) throw new ValidationException("password", "Введите пароль");
        return save("POST", "api/employees", e, password);
    }
    public Employee update(Employee e, String password) { return save("PUT", "api/employees/" + e.id(), e, password); }
    public void archive(UUID id) { api.call("DELETE", "api/employees/" + id, null, Void.class); }
    private Employee save(String method, String path, Employee e, String password) {
        String hash = password == null || password.isBlank() ? null : PasswordHasher.sha256(password);
        return model(api.call(method, path, new ApiModels.EmployeeInput(e.fullName(), e.role(), e.login(), hash, e.isActive()), ApiModels.Employee.class));
    }
    private Employee model(ApiModels.Employee e) { return new Employee(e.id(), e.fullName(), e.role(), e.login(), e.active(), e.createdAt()); }
}
