package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;
import com.katu4a.cladbise.util.PlotStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Заменить моки на реальную бд
 */
public class StubPlotService implements PlotService {

    @Override
    public List<Plot> findAll() {
        return List.of(
                new Plot(1, 1, 2, 1, PlotStatus.RESERVED, 200f, 100f, "55.7558, 37.6173"),
                new Plot(2, 2, 1, 1, PlotStatus.OCCUPIED, 200f, 100f, "28.124, 12.213"),
                new Plot(3, 1, 1, 1, PlotStatus.FREE, 200f, 100f, "69.6767, 67.6969"),
                new Plot(4, 3, 2, 1, PlotStatus.FREE, 200f, 100f, "1.0, 2.0"),
                new Plot(5, 4, 1, 1, PlotStatus.FREE, 200f, 100f, "12.12, 33.222"),
                new Plot(6, 5, 1, 1, PlotStatus.OCCUPIED, 200f, 100f, "99.41234, 66.125")
        );
    }

    @Override
    public List<Sector> findAllSectors() {
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
}