package com.katu4a.cladbise.controller;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;
import com.katu4a.cladbise.service.PlotService;
import com.katu4a.cladbise.service.StubPlotService;
import com.katu4a.cladbise.service.ValidationException;
import com.katu4a.cladbise.util.AlertHelper;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label statusLabel;

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

        editButton.disableProperty().bind(
                plotsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(
                plotsTable.getSelectionModel().selectedItemProperty().isNull());

        plotsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && plotsTable.getSelectionModel().getSelectedItem() != null) {
                onEdit();
            }
        });

        loadData();
    }

    @FXML
    private void onRefresh() {
        loadData();
    }

    @FXML
    private void onAdd() {
        if (openForm(null)) {
            loadData();
        }
    }

    @FXML
    private void onEdit() {
        Plot selected = plotsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (openForm(selected)) {
            loadData();
        }
    }

    @FXML
    private void onDelete() {
        Plot selected = plotsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        String sectorName = sectorNames.getOrDefault(selected.sectorId(), "—");
        boolean confirmed = AlertHelper.confirm(String.format(
                "Удалить место: сектор «%s», ряд %d, место %d?",
                sectorName, selected.rowNumber(), selected.plotNumber()));
        if (!confirmed) {
            return;
        }

        try {
            plotService.delete(selected.id());
            loadData();
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Не удалось удалить место: " + e.getMessage());
        }
    }

    private boolean openForm(Plot existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/plot-form.fxml"));
            Parent root = loader.load();
            PlotFormController controller = loader.getController();
            controller.init(plotService, existing);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage dialog = new Stage();
            dialog.initOwner(plotsTable.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "Добавление места" : "Редактирование места");
            dialog.setResizable(false);
            dialog.setScene(scene);
            dialog.showAndWait();

            return controller.isSaved();
        } catch (IOException e) {
            AlertHelper.showError("Не удалось открыть форму: " + e.getMessage());
            return false;
        }
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