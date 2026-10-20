package com.arnor4eck.service.outputstrategy;

import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.concrete.AllValuesOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.ConcreteValueOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.MenuProviderOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.SortOutputStrategy;
import com.arnor4eck.util.OutputStrategyPair;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static java.util.Collections.singletonList;

public class OutputStrategyFactory {

    private final Scanner scanner;

    public OutputStrategyFactory(Scanner scanner) {
        this.scanner = scanner;
    }

    public static <T> AllValuesOutputStrategy<T> allValues(
            Repository<T> repository
    ) {
        return new AllValuesOutputStrategy<>(repository);
    }

    public <T> ConcreteValueOutputStrategy<T> concreteValue(
            Repository<T> repository
    ) {
        return new ConcreteValueOutputStrategy<>(repository, this.scanner);
    }

    public MenuProviderOutputStrategy menuProvider(
            String topic,
            List<OutputStrategyPair<?>> pairs
    ) {
        return new MenuProviderOutputStrategy(topic, this.scanner, pairs);
    }

    public MenuProviderOutputStrategy menuProvider(
            String topic,
            OutputStrategyPair<?> pair
    ) {
        return new MenuProviderOutputStrategy(topic, this.scanner, singletonList(pair));
    }

    public static <T> SortOutputStrategy<T> sort(
            Repository<T> repository,
            Comparator<T> comparator
    ) {
        return new SortOutputStrategy<>(repository, comparator);
    }
}
