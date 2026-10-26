package com.katu4a.cladbise.service;
import com.katu4a.cladbise.api.*;
import com.katu4a.cladbise.model.*;
import java.util.*;
public final class HttpPlotService implements PlotService {
    private final ApiClient api;
    public HttpPlotService(ApiClient api) { this.api = api; }
    public List<Plot> findAll() {
        return api.list("api/plots", ApiModels.Plot.class).stream()
                .filter(p -> p.status() == ApiModels.Status.ACTIVE).map(this::model).toList();
    }
    public List<Sector> findAllSectors() {
        return api.list("api/sectors", ApiModels.Sector.class).stream()
                .map(s -> new Sector(s.id(), s.name(), s.createdAt(), s.status() == ApiModels.Status.ARCHIVE)).toList();
    }
    public Plot create(Plot p) { return save("POST", "api/plots", p); }
    public Plot update(Plot p) { return save("PUT", "api/plots/" + p.id(), p); }
    public void archive(UUID id) { api.call("DELETE", "api/plots/" + id, null, Void.class); }
    private Plot save(String method, String path, Plot p) {
        return model(api.call(method, path, new ApiModels.PlotInput(p.sectorId(), p.rowNumber(), p.plotNumber(),
                p.status(), p.lengthCm(), p.widthCm(), p.coordinates()), ApiModels.Plot.class));
    }
    private Plot model(ApiModels.Plot p) { return new Plot(p.id(), p.sectorId(), p.rowNumber(), p.plotNumber(), p.plotStatus(), p.lengthCm(), p.widthCm(), p.coordinates()); }
}
