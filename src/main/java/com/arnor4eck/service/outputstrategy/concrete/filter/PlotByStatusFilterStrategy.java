package com.arnor4eck.service.outputstrategy.concrete.filter;

import com.arnor4eck.model.Plot;
import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.concrete.filter.FilterOutputStrategy;
import com.arnor4eck.util.enums.PlotStatus;

import java.util.Scanner;
import java.util.function.Predicate;

public class PlotByStatusFilterStrategy extends FilterOutputStrategy<Plot> {

    private final Scanner scanner;

    public PlotByStatusFilterStrategy(
            Repository<Plot> repository,
            Scanner scanner
    ) {
        super(repository);
        this.scanner = scanner;
    }

    @Override
    public Predicate<Plot> predicate() {
        try {
            scanner.nextLine();
            PlotStatus plotStatus = enterPlotStatus();
            return plot -> plot.status().equals(plotStatus);
        } catch (Exception e) {
            System.out.printf("Не удалось создать фильтр: %s. Выведены все значения\n", e.getMessage());
            return a -> true; // тот же самый
        }
    }

    private PlotStatus enterPlotStatus() {
        System.out.println("Введите одно из значений: ");
        for (var plotStatus : PlotStatus.values()) {
            System.out.printf("%s (%s)\n", plotStatus.name(), plotStatus.getValue());
        }

        return PlotStatus.fromString(scanner.nextLine());
    }
}
