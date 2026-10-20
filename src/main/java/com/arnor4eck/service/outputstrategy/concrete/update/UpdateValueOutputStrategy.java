package com.arnor4eck.service.outputstrategy.concrete.update;

import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.OutputStrategy;
import com.arnor4eck.util.exceptions.CreateValueException;

import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;

public abstract class UpdateValueOutputStrategy<T> implements OutputStrategy {

    private final Repository<T> repository;
    protected final Scanner scanner;

    public UpdateValueOutputStrategy(
            Repository<T> repository,
            Scanner scanner
    ) {
        this.repository = repository;
        this.scanner = scanner;
    }

    @Override
    public String act() {
        try {
            int id = enterPositiveInteger("id");

            Optional<T> optional = repository.get(id);

            if (optional.isEmpty())
                throw new RuntimeException("Не удалось найти сущность с id %d".formatted(id));

            T value = updateValue(optional.get());

            repository.update(value);
            return String.format("Обновленное значение: %s", value);
        } catch (Exception e) {
            return String.format("Ошибка обновления сущности: %s. Прогресс сброшен\n", e.getMessage());
        }
    }

    protected abstract T updateValue(T value) throws CreateValueException;

    protected int enterPositiveInteger(String param) throws CreateValueException {
        try {
            System.out.printf("Введите %s: ", param);
            int num = scanner.nextInt();

            if (num <= 0)
                throw new CreateValueException("Введённое число не может быть меньше 1");

            return num;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            throw new CreateValueException("Некорректно введено число %s".formatted(param));
        } finally {
            scanner.nextLine();
        }
    }
}
