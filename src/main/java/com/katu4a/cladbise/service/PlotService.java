package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;

import java.util.List;
import java.util.UUID;

public interface PlotService {

    List<Plot> findAll();

    List<Sector> findAllSectors();

    Plot create(Plot plot);

    Plot update(Plot plot);

    void archive(UUID id);
}