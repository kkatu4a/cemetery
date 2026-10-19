package com.arnor4eck.service.outputstrategy.concrete.filter;

import com.arnor4eck.model.Customer;
import com.arnor4eck.repository.Repository;

import java.util.Scanner;
import java.util.function.Predicate;

public class CustomerByFullNameFilterStrategy extends FilterOutputStrategy<Customer>{

    private final Scanner scanner;

    public CustomerByFullNameFilterStrategy(
            Repository<Customer> repository,
            Scanner scanner
    ) {
        super(repository);
        this.scanner = scanner;
    }

    @Override
    public Predicate<Customer> predicate() {
        scanner.nextLine();
        System.out.print("Введите набор символов, по которому осуществится поиск: ");

        String line = scanner.nextLine();

        return customer -> customer.fullName().toLowerCase().contains(line.toLowerCase());
    }
}
