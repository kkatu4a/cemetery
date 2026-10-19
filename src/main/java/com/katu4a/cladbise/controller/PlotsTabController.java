package com.katu4a.cladbise.controller;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;
import com.katu4a.cladbise.service.PlotService;
import com.katu4a.cladbise.service.StubPlotService;
import com.katu4a.cladbise.util.AlertHelper;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.Map;

public class PlotsTabController {

    @FXML
    private TableView<Plot> plotsTable;
    @FXML
    private TableColumn<Plot, String> sectorColumn;
    @FXML
    private TableColumn<Plot, Number> rowColumn;
    @FXML
    private TableColumn<Plot, Number> plotNumberColumn;
    @FXML
    private TableColumn<Plot, String> statusColumn;
    @FXML
    private TableColumn<Plot, Number> lengthColumn;
    @FXML
    private TableColumn<Plot, Number> widthColumn;
    @FXML
    private TableColumn<Plot, String> coordinatesColumn;
    @FXML
    private Label statusLabel;

    // заменить на сервис работающий с бд
    private final PlotService plotService = new StubPlotService();

    private final ObservableList<Plot> plots = FXCollections.observableArrayList();
    private final Map<Integer, String> sectorNames = new HashMap<>();

    @FXML
    private void initialize() {
        sectorColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(sectorNames.getOrDefault(cell.getValue().sectorId(), "—")));
        rowColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().rowNumber()));
        plotNumberColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().plotNumber()));
        statusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().status().getValue()));
        lengthColumn.setCellValueFactory(cell ->
                new SimpleFloatProperty(cell.getValue().lengthCm()));
        widthColumn.setCellValueFactory(cell ->
                new SimpleFloatProperty(cell.getValue().widthCm()));
        coordinatesColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().coordinates() == null ? "" : cell.getValue().coordinates()));

        plotsTable.setItems(plots);
        loadData();
    }

    @FXML
    private void onRefresh() {
        loadData();
    }

    private void loadData() {
        try {
            sectorNames.clear();
            for (Sector sector : plotService.findAllSectors()) {
                sectorNames.put(sector.id(), sector.name());
            }
            plots.setAll(plotService.findAll());
            statusLabel.setText("Найдено записей: " + plots.size());
        } catch (Exception e) {
            AlertHelper.showError("Не удалось загрузить места: " + e.getMessage());
        }
    }
}