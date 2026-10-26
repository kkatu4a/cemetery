package com.katu4a.cladbise.controller;
import com.katu4a.cladbise.util.FxAsync;
import com.katu4a.cladbise.util.BackendProvider;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;
import com.katu4a.cladbise.service.PlotService;
import com.katu4a.cladbise.service.ValidationException;
import com.katu4a.cladbise.util.AlertHelper;
import com.katu4a.cladbise.util.FormDialog;
import com.katu4a.cladbise.util.FormUtils;
import com.katu4a.cladbise.util.PlotStatus;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotsTabController {

    private static final String SORT_SECTOR = "Сектор";
    private static final String SORT_ROW = "Ряд";
    private static final String SORT_NUMBER = "Номер места";
    private static final String SORT_STATUS = "Статус";
    private static final String SORT_LENGTH = "Длина";
    private static final String SORT_WIDTH = "Ширина";

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
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<PlotStatus> statusFilter;
    @FXML
    private ComboBox<Sector> sectorFilter;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private CheckBox descendingCheck;

    private final PlotService plotService = BackendProvider.getPlotService();

    private final ObservableList<Plot> plots = FXCollections.observableArrayList();
    private final FilteredList<Plot> filteredPlots = new FilteredList<>(plots);
    private final SortedList<Plot> sortedPlots = new SortedList<>(filteredPlots);
    private final Map<java.util.UUID, String> sectorNames = new HashMap<>();

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

        plotsTable.setItems(sortedPlots);
        sortedPlots.comparatorProperty().bind(plotsTable.comparatorProperty());

        editButton.disableProperty().bind(
                plotsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(
                plotsTable.getSelectionModel().selectedItemProperty().isNull());

        plotsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && plotsTable.getSelectionModel().getSelectedItem() != null) {
                onEdit();
            }
        });

        statusFilter.setConverter(FormUtils.converter(PlotStatus::getValue));
        statusFilter.setItems(FXCollections.observableArrayList(PlotStatus.values()));
        sectorFilter.setConverter(FormUtils.converter(Sector::name));
        sortCombo.setItems(FXCollections.observableArrayList(
                SORT_SECTOR, SORT_ROW, SORT_NUMBER, SORT_STATUS, SORT_LENGTH, SORT_WIDTH));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        sectorFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        sortCombo.valueProperty().addListener((observable, oldValue, newValue) -> applySort());
        descendingCheck.selectedProperty().addListener((observable, oldValue, newValue) -> applySort());

        loadData();
    }

    @FXML
    private void onRefresh() {
        loadData();
    }

    @FXML
    private void onReset() {
        searchField.clear();
        statusFilter.setValue(null);
        sectorFilter.setValue(null);
        sortCombo.setValue(null);
        descendingCheck.setSelected(false);
        plotsTable.getSortOrder().clear();
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

        FxAsync.run(plotsTable, () -> { plotService.delete(selected.id()); return null; },
                ignored -> loadData(), error -> AlertHelper.showError(error.getMessage()));
    }

    private boolean openForm(Plot existing) {
        return FormDialog.<PlotFormController>show(
                plotsTable.getScene().getWindow(),
                "/fxml/plot-form.fxml",
                existing == null ? "Добавление места" : "Редактирование места",
                controller -> controller.init(plotService, existing));
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        PlotStatus status = statusFilter.getValue();
        Sector sector = sectorFilter.getValue();

        filteredPlots.setPredicate(plot -> matches(plot, query, status, sector));
        updateStatusLabel();
    }

    private boolean matches(Plot plot, String query, PlotStatus status, Sector sector) {
        if (status != null && plot.status() != status) {
            return false;
        }
        if (sector != null && !plot.sectorId().equals(sector.id())) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        List<String> searchable = List.of(
                sectorNames.getOrDefault(plot.sectorId(), ""),
                String.valueOf(plot.rowNumber()),
                String.valueOf(plot.plotNumber()),
                plot.status().getValue(),
                plot.coordinates() == null ? "" : plot.coordinates());
        return searchable.stream().anyMatch(value -> value.toLowerCase().contains(query));
    }

    private void applySort() {
        String selected = sortCombo.getValue();
        if (selected == null) {
            plotsTable.getSortOrder().clear();
            return;
        }

        TableColumn<Plot, ?> column = switch (selected) {
            case SORT_SECTOR -> sectorColumn;
            case SORT_ROW -> rowColumn;
            case SORT_NUMBER -> plotNumberColumn;
            case SORT_STATUS -> statusColumn;
            case SORT_LENGTH -> lengthColumn;
            case SORT_WIDTH -> widthColumn;
            default -> null;
        };
        if (column == null) {
            return;
        }

        column.setSortType(descendingCheck.isSelected()
                ? TableColumn.SortType.DESCENDING
                : TableColumn.SortType.ASCENDING);
        plotsTable.getSortOrder().setAll(List.of(column));
    }

    private void updateStatusLabel() {
        statusLabel.setText(String.format("Показано записей: %d из %d", filteredPlots.size(), plots.size()));
    }

    private record PlotData(List<Sector> sectors, List<Plot> plots) {}

    private void loadData() {
        statusLabel.setText("Загрузка…");
        FxAsync.run(plotsTable, () -> new PlotData(plotService.findAllSectors(), plotService.findAll()), data -> {
            sectorNames.clear();
            data.sectors().forEach(sector -> sectorNames.put(sector.id(), sector.name()));
            Sector previous = sectorFilter.getValue();
            sectorFilter.setItems(FXCollections.observableArrayList(data.sectors()));
            if (previous != null) data.sectors().stream().filter(sector -> sector.id().equals(previous.id()))
                    .findFirst().ifPresent(sectorFilter::setValue);
            plots.setAll(data.plots()); applyFilter();
        }, error -> { statusLabel.setText("Ошибка загрузки"); AlertHelper.showError(error.getMessage()); });
    }
}