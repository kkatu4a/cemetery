package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;

import java.util.List;

public interface PlotService {

    List<Plot> findAll();

    List<Sector> findAllSectors();
}