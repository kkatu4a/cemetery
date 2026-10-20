package com.arnor4eck.service.outputstrategy.concrete.filter;

import com.arnor4eck.model.Customer;
import com.arnor4eck.repository.Repository;

import java.util.Scanner;
import java.util.function.Predicate;

public class CustomerByPhoneFilterStrategy extends FilterOutputStrategy<Customer>{

    private final Scanner scanner;

    public CustomerByPhoneFilterStrategy(
            Repository<Customer> repository,
            Scanner scanner
    ) {
        super(repository);
        this.scanner = scanner;
    }

    @Override
    public Predicate<Customer> predicate() {
        scanner.nextLine();
        System.out.print("Введите номер клиента: ");

        String line = scanner.nextLine();

        return customer -> customer.phone().equals(line);
    }
}
