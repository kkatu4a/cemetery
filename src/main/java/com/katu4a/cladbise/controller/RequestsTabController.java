package com.katu4a.cladbise.controller;
import com.katu4a.cladbise.util.FxAsync;
import com.katu4a.cladbise.util.BackendProvider;

import com.katu4a.cladbise.util.RequestStatus;
import com.katu4a.cladbise.model.RequestRow;
import com.katu4a.cladbise.service.RequestService;
import com.katu4a.cladbise.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestsTabController {

    @FXML private TableView<RequestRow> requestsTable;
    @FXML private TableColumn<RequestRow, java.util.UUID> colId;
    @FXML private TableColumn<RequestRow, String> colDeceased;
    @FXML private TableColumn<RequestRow, String> colCustomer;
    @FXML private TableColumn<RequestRow, String> colPlot;
    @FXML private TableColumn<RequestRow, String> colEmployee;
    @FXML private TableColumn<RequestRow, String> colStatus;
    @FXML private TableColumn<RequestRow, String> colDate;
    @FXML private TableColumn<RequestRow, java.math.BigDecimal> colCost;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private DatePicker dpDateFrom;
    @FXML private DatePicker dpDateTo;

    private final RequestService requestService = BackendProvider.getRequestService();
    private final ObservableList<RequestRow> masterData = FXCollections.observableArrayList();
    private FilteredList<RequestRow> filteredData;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDeceased.setCellValueFactory(new PropertyValueFactory<>("deceasedFullName"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerFullName"));
        colPlot.setCellValueFactory(new PropertyValueFactory<>("plotAddress"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeFullName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusRussian"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(dateFormatter));
            }
            return new SimpleStringProperty("—");
        });

        List<String> statuses = new ArrayList<>();
        statuses.add("Все");
        for (RequestStatus st : RequestStatus.values()) {
            statuses.add(st.getValue());
        }
        cbStatusFilter.setItems(FXCollections.observableArrayList(statuses));
        cbStatusFilter.setValue("Все");

        setupFilteringAndSorting();
        loadData();
    }

    private void setupFilteringAndSorting() {
        filteredData = new FilteredList<>(masterData, p -> true);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dpDateFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dpDateTo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        SortedList<RequestRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(requestsTable.comparatorProperty());
        requestsTable.setItems(sortedData);
    }

    private void applyFilters() {
        String search = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String status = cbStatusFilter.getValue();
        LocalDate from = dpDateFrom.getValue();
        LocalDate to = dpDateTo.getValue();

        filteredData.setPredicate(req -> {
            boolean matchSearch = search.isEmpty()
                    || (req.getDeceasedFullName() != null && req.getDeceasedFullName().toLowerCase().contains(search))
                    || (req.getCustomerFullName() != null && req.getCustomerFullName().toLowerCase().contains(search));

            boolean matchStatus = status == null || status.equals("Все")
                    || (req.getStatusRussian() != null && req.getStatusRussian().equals(status));

            boolean matchDate = true;
            if (req.getCreatedAt() != null) {
                LocalDate reqDate = req.getCreatedAt().toLocalDate();
                if (from != null && reqDate.isBefore(from)) matchDate = false;
                if (to != null && reqDate.isAfter(to)) matchDate = false;
            } else if (from != null || to != null) {
                matchDate = false;
            }

            return matchSearch && matchStatus && matchDate;
        });
    }

    @FXML
    public void loadData() {
        FxAsync.run(requestsTable, requestService::findAll, masterData::setAll,
                error -> AlertHelper.showError("Ошибка загрузки", error.getMessage()));
    }

    @FXML
    private void handleResetFilters() {
        txtSearch.clear();
        cbStatusFilter.setValue("Все");
        dpDateFrom.setValue(null);
        dpDateTo.setValue(null);
    }

    @FXML
    private void handleAddRequest() {
        openDialog(null, "Добавление заявки");
    }

    @FXML
    private void handleEditRequest() {
        RequestRow selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Выберите заявку в таблице для редактирования.");
            return;
        }
        openDialog(selected, "Редактирование заявки #" + selected.getId());
    }

    @FXML
    private void handleDeleteRequest() {
        RequestRow selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Выберите заявку для архивирования.");
            return;
        }

        if (AlertHelper.showConfirmation("Архивирование заявки",
                "Отправить в архив заявку #" + selected.getId() + " (" + selected.getDeceasedFullName() + ")?")) {
            FxAsync.run(requestsTable, () -> { requestService.archive(selected.getId()); return null; },
                    ignored -> loadData(), error -> AlertHelper.showError("Ошибка архивации", error.getMessage()));
        }
    }

    @FXML
    private void handleExport() {
        if (filteredData.isEmpty()) {
            AlertHelper.showWarning("Таблица пуста, нет данных для экспорта.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт заявок");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel файлы (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV файлы (*.csv)", "*.csv")
        );
        fileChooser.setInitialFileName("requests_export.xlsx");

        File file = fileChooser.showSaveDialog(requestsTable.getScene().getWindow());
        if (file != null) {
            List<RequestRow> snapshot = List.copyOf(filteredData);
            FxAsync.run(requestsTable, () -> { requestService.exportFile(file, snapshot); return null; },
                    ignored -> AlertHelper.showInfo("Экспорт выполнен", "Файл сохранён: " + file.getAbsolutePath()),
                    error -> AlertHelper.showError("Ошибка экспорта", error.getMessage()));
        }
    }

    private void openDialog(RequestRow row, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/request-dialog.fxml"));
            Parent root = loader.load();

            RequestDialogController controller = loader.getController();
            controller.setRequestRow(row);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(requestsTable.getScene().getWindow());
            Scene dialogScene = new Scene(root);
            dialogScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(dialogScene);
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadData();
            }
        } catch (Exception e) {
            AlertHelper.showError("Ошибка открытия формы", e.getMessage());
        }
    }
}