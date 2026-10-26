package com.katu4a.cladbise.controller;
import com.katu4a.cladbise.util.FxAsync;
import com.katu4a.cladbise.util.BackendProvider;

import com.katu4a.cladbise.model.Employee;
import com.katu4a.cladbise.service.EmployeeService;
import com.katu4a.cladbise.service.ValidationException;
import com.katu4a.cladbise.util.AlertHelper;
import com.katu4a.cladbise.util.FormDialog;
import com.katu4a.cladbise.util.TableFormats;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.util.List;

public class EmployeesTabController {

    @FXML
    private TableView<Employee> employeesTable;
    @FXML
    private TableColumn<Employee, String> fullNameColumn;
    @FXML
    private TableColumn<Employee, String> roleColumn;
    @FXML
    private TableColumn<Employee, String> loginColumn;
    @FXML
    private TableColumn<Employee, String> activeColumn;
    @FXML
    private TableColumn<Employee, LocalDateTime> createdColumn;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;

    private final EmployeeService employeeService = BackendProvider.getEmployeeService();

    private final ObservableList<Employee> employees = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredEmployees = new FilteredList<>(employees);
    private final SortedList<Employee> sortedEmployees = new SortedList<>(filteredEmployees);

    @FXML
    private void initialize() {
        fullNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().fullName()));
        roleColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().role().name()));
        loginColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().login()));
        activeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isActive() ? "Да" : "Нет"));
        createdColumn.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().createdAt()));
        createdColumn.setCellFactory(TableFormats.dateTimeCellFactory());

        employeesTable.setItems(sortedEmployees);
        sortedEmployees.comparatorProperty().bind(employeesTable.comparatorProperty());

        editButton.disableProperty().bind(
                employeesTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(
                employeesTable.getSelectionModel().selectedItemProperty().isNull());

        employeesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && employeesTable.getSelectionModel().getSelectedItem() != null) {
                onEdit();
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());

        loadData();
    }

    @FXML
    private void onRefresh() {
        loadData();
    }

    @FXML
    private void onReset() {
        searchField.clear();
    }

    @FXML
    private void onAdd() {
        if (openForm(null)) {
            loadData();
        }
    }

    @FXML
    private void onEdit() {
        Employee selected = employeesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (openForm(selected)) {
            loadData();
        }
    }

    @FXML
    private void onDelete() {
        Employee selected = employeesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        boolean confirmed = AlertHelper.confirm(
                String.format("Отправить сотрудника в архив «%s»?", selected.fullName()));
        if (!confirmed) {
            return;
        }

        FxAsync.run(employeesTable, () -> { employeeService.archive(selected.id()); return null; },
                ignored -> loadData(), error -> AlertHelper.showError(error.getMessage()));
    }

    private boolean openForm(Employee existing) {
        return FormDialog.<EmployeeFormController>show(
                employeesTable.getScene().getWindow(),
                "/fxml/employee-form.fxml",
                existing == null ? "Добавление сотрудника" : "Редактирование сотрудника",
                controller -> controller.init(employeeService, existing));
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredEmployees.setPredicate(employee -> {
            if (query.isEmpty()) {
                return true;
            }
            List<String> searchable = List.of(employee.fullName(), employee.login());
            return searchable.stream().anyMatch(value -> value.toLowerCase().contains(query));
        });
        statusLabel.setText(String.format("Показано записей: %d из %d",
                filteredEmployees.size(), employees.size()));
    }

    private void loadData() {
        statusLabel.setText("Загрузка…");
        FxAsync.run(employeesTable, employeeService::findAll, values -> {
            employees.setAll(values); applyFilter();
        }, error -> { statusLabel.setText("Ошибка загрузки"); AlertHelper.showError("Не удалось загрузить сотрудников: " + error.getMessage()); });
    }
}