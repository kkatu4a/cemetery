package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;
import com.katu4a.cladbise.util.PlotStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StubPlotService implements PlotService {

    private final List<Sector> sectors = createSectors();

    private final List<Plot> plots = new ArrayList<>(List.of(
            new Plot(1, 1, 2, 1, PlotStatus.RESERVED, 200f, 100f, "55.7558, 37.6173"),
            new Plot(2, 2, 1, 1, PlotStatus.OCCUPIED, 200f, 100f, "28.124, 12.213"),
            new Plot(3, 1, 1, 1, PlotStatus.FREE, 200f, 100f, "69.6767, 67.6969"),
            new Plot(4, 3, 2, 1, PlotStatus.FREE, 200f, 100f, "1.0, 2.0"),
            new Plot(5, 4, 1, 1, PlotStatus.FREE, 200f, 100f, "12.12, 33.222"),
            new Plot(6, 5, 1, 1, PlotStatus.OCCUPIED, 200f, 100f, "99.41234, 66.125")
    ));

    private int nextId = 7;

    private static List<Sector> createSectors() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                new Sector(1, "Весенний", now),
                new Sector(2, "Летний", now),
                new Sector(3, "Зимний", now),
                new Sector(4, "Осенний", now),
                new Sector(5, "Арзамас", now),
                new Sector(6, "Бердск", now)
        );
    }

    @Override
    public List<Plot> findAll() {
        return List.copyOf(plots);
    }

    @Override
    public List<Sector> findAllSectors() {
        return sectors;
    }

    @Override
    public Plot create(Plot plot) {
        validate(plot);
        Plot saved = new Plot(nextId++, plot.sectorId(), plot.rowNumber(), plot.plotNumber(),
                plot.status(), plot.lengthCm(), plot.widthCm(), plot.coordinates());
        plots.add(saved);
        return saved;
    }

    @Override
    public Plot update(Plot plot) {
        validate(plot);
        for (int i = 0; i < plots.size(); i++) {
            if (plots.get(i).id() == plot.id()) {
                plots.set(i, plot);
                return plot;
            }
        }
        throw new ValidationException("Место не найдено, возможно, оно уже удалено");
    }

    @Override
    public void delete(int id) {
        boolean removed = plots.removeIf(plot -> plot.id() == id);
        if (!removed) {
            throw new ValidationException("Место не найдено, возможно, оно уже удалено");
        }
    }

    private void validate(Plot plot) {
        if (plot.status() == null) {
            throw new ValidationException("Статус: выберите статус места");
        }
        boolean sectorExists = sectors.stream().anyMatch(sector -> sector.id() == plot.sectorId());
        if (!sectorExists) {
            throw new ValidationException("Сектор: выбранный сектор не существует");
        }
        if (plot.rowNumber() <= 0) {
            throw new ValidationException("Ряд: значение должно быть больше 0");
        }
        if (plot.plotNumber() <= 0) {
            throw new ValidationException("Номер места: значение должно быть больше 0");
        }
        if (plot.lengthCm() <= 0) {
            throw new ValidationException("Длина: значение должно быть больше 0");
        }
        if (plot.widthCm() <= 0) {
            throw new ValidationException("Ширина: значение должно быть больше 0");
        }
        if (plot.coordinates() != null && plot.coordinates().length() > 100) {
            throw new ValidationException("Координаты: не длиннее 100 символов");
        }
        boolean duplicate = plots.stream().anyMatch(other ->
                other.id() != plot.id()
                        && other.sectorId() == plot.sectorId()
                        && other.rowNumber() == plot.rowNumber()
                        && other.plotNumber() == plot.plotNumber());
        if (duplicate) {
            throw new ValidationException("Место с таким сектором, рядом и номером уже существует");
        }
    }
}