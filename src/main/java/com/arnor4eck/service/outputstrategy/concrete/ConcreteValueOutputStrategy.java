package com.arnor4eck.service.outputstrategy.concrete;

import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.OutputStrategy;

import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;

public class ConcreteValueOutputStrategy<T> implements OutputStrategy {

    private final Repository<T> repository;
    private final Scanner scanner;

    public ConcreteValueOutputStrategy(
        Repository<T> repository,
        Scanner scanner
    ) {
        this.repository = repository;
        this.scanner = scanner;
    }

    @Override
    public String act() {
        try {
            System.out.print("Введите id: ");
            int id = scanner.nextInt();
            Optional<T> val = repository.get(id);

            return val.map(Object::toString)
                    .orElse("Значение не найдено.");
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return "Некорректный ввод. Введённый символ не является числом";
        } catch (SQLException e) {
            return "Не удалось получить сущность: %s\n".formatted(e.getMessage());
        }
    }
}
