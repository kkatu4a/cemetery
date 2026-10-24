package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Employee;
import com.katu4a.cladbise.util.BackendProvider;
import com.katu4a.cladbise.util.Role;

import java.time.LocalDateTime;
import java.util.List;

public class DbEmployeeService implements EmployeeService {
    private final StubEmployeeService stubFallback = new StubEmployeeService();

    private boolean isDbAvailable() {
        try {
            return BackendProvider.getDataBase() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Employee> findAll() {
        if (!isDbAvailable()) {
            return stubFallback.findAll();
        }
        try {
            return BackendProvider.getEmployeeRepository().getAll().stream()
                    .map(e -> new Employee(
                            e.id(),
                            e.fullName(),
                            Role.valueOf(e.role().name()),
                            e.login(),
                            e.passwordHash(),
                            e.isActive(),
                            e.createdAt()
                    ))
                    .toList();
        } catch (Exception e) {
            return stubFallback.findAll();
        }
    }

    @Override
    public Employee create(Employee employee, String rawPassword) {
        if (!isDbAvailable()) {
            return stubFallback.create(employee, rawPassword);
        }
        try {
            String hash = PasswordHasher.sha256(rawPassword != null ? rawPassword : "");
            com.arnor4eck.model.Employee dbModel = new com.arnor4eck.model.Employee(
                    employee.id(),
                    employee.fullName(),
                    com.arnor4eck.util.enums.Role.fromString(employee.role().name()),
                    employee.login(),
                    hash,
                    employee.isActive(),
                    employee.createdAt() != null ? employee.createdAt() : LocalDateTime.now()
            );
            BackendProvider.getEmployeeRepository().save(dbModel);
            return findAll().stream()
                    .filter(e -> e.login().equals(employee.login()))
                    .reduce((first, second) -> second)
                    .orElse(employee);
        } catch (Exception e) {
            return stubFallback.create(employee, rawPassword);
        }
    }

    @Override
    public Employee update(Employee employee, String rawPassword) {
        if (!isDbAvailable()) {
            return stubFallback.update(employee, rawPassword);
        }
        try {
            String hash = (rawPassword != null && !rawPassword.isBlank())
                    ? PasswordHasher.sha256(rawPassword)
                    : employee.passwordHash();
            com.arnor4eck.model.Employee dbModel = new com.arnor4eck.model.Employee(
                    employee.id(),
                    employee.fullName(),
                    com.arnor4eck.util.enums.Role.fromString(employee.role().name()),
                    employee.login(),
                    hash,
                    employee.isActive(),
                    employee.createdAt() != null ? employee.createdAt() : LocalDateTime.now()
            );
            BackendProvider.getEmployeeRepository().update(dbModel);
            return employee;
        } catch (Exception e) {
            return stubFallback.update(employee, rawPassword);
        }
    }

    @Override
    public void delete(int id) {
        if (!isDbAvailable()) {
            stubFallback.delete(id);
            return;
        }
        try {
            BackendProvider.getEmployeeRepository().delete(id);
        } catch (Exception e) {
            stubFallback.delete(id);
        }
    }
}
