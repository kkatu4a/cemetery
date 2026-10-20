package com.arnor4eck.service.outputstrategy.concrete.filter;

import com.arnor4eck.model.Employee;
import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.concrete.filter.FilterOutputStrategy;

import java.util.Scanner;
import java.util.function.Predicate;

public class EmployeeByLoginFilterStrategy extends FilterOutputStrategy<Employee> {

    private final Scanner scanner;

    public EmployeeByLoginFilterStrategy(
            Repository<Employee> repository,
            Scanner scanner
    ) {
        super(repository);
        this.scanner = scanner;
    }

    @Override
    public Predicate<Employee> predicate() {
        scanner.nextLine();
        System.out.print("Введите набор символов, по которому осуществится поиск: ");

        String line = scanner.nextLine();

        return employee -> employee.login().toLowerCase().contains(line.toLowerCase());
    }
}
