package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;
import com.katu4a.cladbise.util.BackendProvider;
import com.katu4a.cladbise.util.PlotStatus;

import java.util.List;

public class DbPlotService implements PlotService {
    private final StubPlotService stubFallback = new StubPlotService();

    private boolean isDbAvailable() {
        try {
            return BackendProvider.getDataBase() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Plot> findAll() {
        if (!isDbAvailable()) {
            return stubFallback.findAll();
        }
        try {
            return BackendProvider.getPlotRepository().getAll().stream()
                    .map(p -> new Plot(
                            p.id(),
                            p.sectorId(),
                            p.rowNumber(),
                            p.plotNumber(),
                            PlotStatus.valueOf(p.status().name()),
                            p.lengthCm(),
                            p.widthCm(),
                            p.coordinates()
                    ))
                    .toList();
        } catch (Exception e) {
            return stubFallback.findAll();
        }
    }

    @Override
    public List<Sector> findAllSectors() {
        if (!isDbAvailable()) {
            return stubFallback.findAllSectors();
        }
        try {
            return BackendProvider.getSectorRepository().getAll().stream()
                    .map(s -> new Sector(s.id(), s.name(), s.createdAt()))
                    .toList();
        } catch (Exception e) {
            return stubFallback.findAllSectors();
        }
    }

    @Override
    public Plot create(Plot plot) {
        if (!isDbAvailable()) {
            return stubFallback.create(plot);
        }
        try {
            com.arnor4eck.model.Plot dbModel = new com.arnor4eck.model.Plot(
                    plot.id(),
                    plot.sectorId(),
                    plot.rowNumber(),
                    plot.plotNumber(),
                    com.arnor4eck.util.enums.PlotStatus.fromString(plot.status().name()),
                    plot.lengthCm(),
                    plot.widthCm(),
                    plot.coordinates()
            );
            BackendProvider.getPlotRepository().save(dbModel);
            return findAll().stream()
                    .filter(p -> p.sectorId() == plot.sectorId() && p.rowNumber() == plot.rowNumber() && p.plotNumber() == plot.plotNumber())
                    .findFirst()
                    .orElse(plot);
        } catch (Exception e) {
            return stubFallback.create(plot);
        }
    }

    @Override
    public Plot update(Plot plot) {
        if (!isDbAvailable()) {
            return stubFallback.update(plot);
        }
        try {
            com.arnor4eck.model.Plot dbModel = new com.arnor4eck.model.Plot(
                    plot.id(),
                    plot.sectorId(),
                    plot.rowNumber(),
                    plot.plotNumber(),
                    com.arnor4eck.util.enums.PlotStatus.fromString(plot.status().name()),
                    plot.lengthCm(),
                    plot.widthCm(),
                    plot.coordinates()
            );
            BackendProvider.getPlotRepository().update(dbModel);
            return plot;
        } catch (Exception e) {
            return stubFallback.update(plot);
        }
    }

    @Override
    public void delete(int id) {
        if (!isDbAvailable()) {
            stubFallback.delete(id);
            return;
        }
        try {
            BackendProvider.getPlotRepository().delete(id);
        } catch (Exception e) {
            stubFallback.delete(id);
        }
    }
}
