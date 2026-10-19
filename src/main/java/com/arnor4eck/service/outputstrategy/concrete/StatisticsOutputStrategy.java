package com.arnor4eck.service.outputstrategy.concrete;

import com.arnor4eck.model.Plot;
import com.arnor4eck.model.Request;
import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.OutputStrategy;
import com.arnor4eck.util.enums.PlotStatus;
import com.arnor4eck.util.enums.RequestStatus;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public class StatisticsOutputStrategy implements OutputStrategy {

    private final Repository<Plot> plotRepository;
    private final Repository<Request> requestRepository;

    public StatisticsOutputStrategy(
            Repository<Plot> plotRepository,
            Repository<Request> requestRepository
    ) {
        this.plotRepository = plotRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public String act() {

        return String.join("\n", getPlotStatistics(), getRequestStatistics());
    }

    private String getPlotStatistics() {
        Collection<Plot> all;
        try {
            all = plotRepository.getAll();
        } catch (SQLException e) {
            return "Не удалось получить все сущности: %s\n".formatted(e.getMessage());
        }

        String[] array = Arrays.stream(PlotStatus.values())
                .map(status -> filter(all, status.getValue(), plot -> plot.status().equals(status)))
                .toArray(String[]::new);

        return String.format("Всего мест: %d; ", all.size()) + String.join("; ", array);
    }

    private <T> String filter(Collection<T> all, String name, Predicate<T> predicate) {
        long counted = all.stream()
                .filter(predicate)
                .count();
        return String.format("%s - %d", name, counted);
    }

    private String getRequestStatistics() {
        Collection<Request> all;
        try {
            all = requestRepository.getAll();
        } catch (SQLException e) {
            return "Не удалось получить все сущности: %s\n".formatted(e.getMessage());
        }

        String[] array = Arrays.stream(RequestStatus.values())
                .map(status -> filter(all, status.getValue(), val -> val.status().equals(status)))
                .toArray(String[]::new);

        return String.format("Всего запросов: %d; ", all.size()) + String.join("; ", array);
    }
}
