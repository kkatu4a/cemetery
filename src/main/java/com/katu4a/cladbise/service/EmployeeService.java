package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Employee;

import java.util.List;

public interface EmployeeService {

    List<Employee> findAll();

    Employee create(Employee employee, String rawPassword);

    Employee update(Employee employee, String rawPassword);

    void delete(int id);
}