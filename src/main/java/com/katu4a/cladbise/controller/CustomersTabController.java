package com.katu4a.cladbise.controller;
import com.katu4a.cladbise.util.FxAsync;
import com.katu4a.cladbise.util.BackendProvider;

import com.katu4a.cladbise.model.Customer;
import com.katu4a.cladbise.service.CustomerService;
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

public class CustomersTabController {

    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableColumn<Customer, String> fullNameColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    @FXML
    private TableColumn<Customer, String> emailColumn;
    @FXML
    private TableColumn<Customer, LocalDateTime> createdColumn;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;

    private final CustomerService customerService = BackendProvider.getCustomerService();

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final FilteredList<Customer> filteredCustomers = new FilteredList<>(customers);
    private final SortedList<Customer> sortedCustomers = new SortedList<>(filteredCustomers);

    @FXML
    private void initialize() {
        fullNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().fullName()));
        phoneColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().phone()));
        emailColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().email() == null ? "" : cell.getValue().email()));
        createdColumn.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().createdAt()));
        createdColumn.setCellFactory(TableFormats.dateTimeCellFactory());

        customersTable.setItems(sortedCustomers);
        sortedCustomers.comparatorProperty().bind(customersTable.comparatorProperty());

        editButton.disableProperty().bind(
                customersTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(
                customersTable.getSelectionModel().selectedItemProperty().isNull());

        customersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && customersTable.getSelectionModel().getSelectedItem() != null) {
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
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (openForm(selected)) {
            loadData();
        }
    }

    @FXML
    private void onDelete() {
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        boolean confirmed = AlertHelper.confirm(
                String.format("Удалить заявителя «%s»?", selected.fullName()));
        if (!confirmed) {
            return;
        }

        FxAsync.run(customersTable, () -> { customerService.delete(selected.id()); return null; },
                ignored -> loadData(), error -> AlertHelper.showError(error.getMessage()));
    }

    private boolean openForm(Customer existing) {
        return FormDialog.<CustomerFormController>show(
                customersTable.getScene().getWindow(),
                "/fxml/customer-form.fxml",
                existing == null ? "Добавление заявителя" : "Редактирование заявителя",
                controller -> controller.init(customerService, existing));
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredCustomers.setPredicate(customer -> {
            if (query.isEmpty()) {
                return true;
            }
            List<String> searchable = List.of(
                    customer.fullName(),
                    customer.phone(),
                    customer.email() == null ? "" : customer.email());
            return searchable.stream().anyMatch(value -> value.toLowerCase().contains(query));
        });
        statusLabel.setText(String.format("Показано записей: %d из %d",
                filteredCustomers.size(), customers.size()));
    }

    private void loadData() {
        statusLabel.setText("Загрузка…");
        FxAsync.run(customersTable, customerService::findAll, values -> {
            customers.setAll(values); applyFilter();
        }, error -> { statusLabel.setText("Ошибка загрузки"); AlertHelper.showError("Не удалось загрузить заявителей: " + error.getMessage()); });
    }
}