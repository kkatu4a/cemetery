package com.arnor4eck.service.outputstrategy.concrete;

import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.OutputStrategy;

import java.sql.SQLException;
import java.util.stream.Collectors;

public class AllValuesOutputStrategy<T> implements OutputStrategy {

    public final Repository<T> repository;

    public AllValuesOutputStrategy(Repository<T> repository) {
        this.repository = repository;
    }

    @Override
    public String act() {
        try {
            return repository.getAll()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
        } catch (SQLException e) {
            return "Не удалось получить все сущности: %s\n".formatted(e.getMessage());
        }
    }
}
