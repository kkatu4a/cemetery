package com.arnor4eck.service.outputstrategy.concrete.create;

import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.OutputStrategy;
import com.arnor4eck.util.exceptions.CreateValueException;

import java.util.InputMismatchException;
import java.util.Scanner;

public abstract class CreateValueOutputStrategy<T> implements OutputStrategy {

    protected final Repository<T> repository;
    protected final Scanner scanner;

    public CreateValueOutputStrategy(
            Repository<T> repository,
            Scanner scanner
    ) {
        this.repository = repository;
        this.scanner = scanner;
    }

    protected int enterPositiveInteger(String param) throws CreateValueException {
        try {
            System.out.printf("Введите %s: ", param);
            int num = scanner.nextInt();

            if(num <= 0)
                throw new CreateValueException("Введённое число не может быть меньше 1");

            return num;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            throw new CreateValueException("Некорректно введено число %s".formatted(param));
        } finally {
            scanner.nextLine();
        }
    }

    protected float enterPositiveFloat(String param) throws CreateValueException {
        try {
            System.out.printf("Введите %s: ", param);
            float num = scanner.nextFloat();

            if(num <= 0)
                throw new CreateValueException("Введённое число не может быть меньше или равно 0");

            return num;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            throw new CreateValueException("Некорректно введено число %s".formatted(param));
        } finally {
            scanner.nextLine();
        }
    }
}
