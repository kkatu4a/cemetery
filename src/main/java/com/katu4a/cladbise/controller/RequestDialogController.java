package com.katu4a.cladbise.controller;
import com.katu4a.cladbise.util.BackendProvider;

import com.katu4a.cladbise.util.RequestStatus;
import com.katu4a.cladbise.service.ValidationException;
import com.katu4a.cladbise.model.ComboItem;
import com.katu4a.cladbise.model.RequestRow;
import com.katu4a.cladbise.service.RequestService;
import com.katu4a.cladbise.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class RequestDialogController {

    @FXML private ComboBox<ComboItem> cbCustomer;
    @FXML private ComboBox<ComboItem> cbPlot;
    @FXML private ComboBox<ComboItem> cbEmployee;
    @FXML private TextField txtDeceasedName;
    @FXML private DatePicker dpBirthDate;
    @FXML private DatePicker dpDeathDate;
    @FXML private TextField txtCertificate;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField txtCost;
    @FXML private TextArea txtNotes;

    private final RequestService requestService = BackendProvider.getRequestService();
    private RequestRow requestRow;
    private boolean saved = false;

    @FXML
    public void initialize() {
        // Заполняем выпадающий список статусов русскими значениями из RequestStatus
        List<String> statusNames = Arrays.stream(RequestStatus.values())
                .map(RequestStatus::getValue)
                .toList();
        cbStatus.setItems(FXCollections.observableArrayList(statusNames));
        cbStatus.setValue(RequestStatus.NEW.getValue());
    }

    public void setRequestRow(RequestRow row) {
        this.requestRow = row;
        try {
            List<ComboItem> customers = requestService.getCustomersForCombo();
            List<ComboItem> employees = requestService.getEmployeesForCombo();
            List<ComboItem> plots = requestService.getPlotsForCombo(row != null ? row.getPlotId() : null);

            cbCustomer.setItems(FXCollections.observableArrayList(customers));
            cbEmployee.setItems(FXCollections.observableArrayList(employees));
            cbPlot.setItems(FXCollections.observableArrayList(plots));

            if (row != null) {
                txtDeceasedName.setText("— (Бронь)".equals(row.getDeceasedFullName()) ? "" : row.getDeceasedFullName());
                dpBirthDate.setValue(row.getDeceasedBirthday());
                dpDeathDate.setValue(row.getDeceasedDeathday());
                txtCertificate.setText(row.getDeceasedCertificate());
                cbStatus.setValue(row.getStatusRussian());
                txtCost.setText(row.getTotalCost() != null ? String.valueOf(row.getTotalCost()) : "0.0");
                txtNotes.setText(row.getNote()); // Исправлено с getNotes на getNote

                customers.stream().filter(c -> c.id().equals(row.getCustomerId())).findFirst().ifPresent(cbCustomer::setValue);
                employees.stream().filter(e -> e.id().equals(row.getEmployeeId())).findFirst().ifPresent(cbEmployee::setValue);
                plots.stream().filter(p -> p.id().equals(row.getPlotId())).findFirst().ifPresent(cbPlot::setValue);
            }
        } catch (Exception e) {
            AlertHelper.showError("Ошибка справочников", e.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        try {
            if (requestRow == null) {
                requestRow = new RequestRow();
            }

            requestRow.setCustomerId(cbCustomer.getValue() != null ? cbCustomer.getValue().id() : null);
            requestRow.setPlotId(cbPlot.getValue() != null ? cbPlot.getValue().id() : null);
            requestRow.setEmployeeId(cbEmployee.getValue() != null ? cbEmployee.getValue().id() : null);
            requestRow.setDeceasedFullName(txtDeceasedName.getText() != null ? txtDeceasedName.getText().trim() : "");
            requestRow.setDeceasedBirthday(dpBirthDate.getValue());
            requestRow.setDeceasedDeathday(dpDeathDate.getValue());
            requestRow.setDeceasedCertificate(txtCertificate.getText() != null ? txtCertificate.getText().trim() : "");

            // Определяем статус enum по выбранному русскому тексту
            String selectedRuStatus = cbStatus.getValue();
            RequestStatus statusEnum = Arrays.stream(RequestStatus.values())
                    .filter(s -> s.getValue().equals(selectedRuStatus))
                    .findFirst()
                    .orElse(RequestStatus.NEW);
            requestRow.setStatus(statusEnum);

            String costStr = txtCost.getText() != null ? txtCost.getText().trim().replace(',', '.') : "0";
            try {
                requestRow.setTotalCost(costStr.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(costStr));
            } catch (NumberFormatException e) {
                throw new ValidationException("Стоимость должна быть числом!");
            }

            requestRow.setNote(txtNotes.getText() != null ? txtNotes.getText().trim() : ""); // Исправлено на setNote

            if (requestRow.getId() == null) {
                requestService.create(requestRow);
            } else {
                requestService.update(requestRow);
            }

            saved = true;
            closeStage();
        } catch (ValidationException ve) {
            AlertHelper.showError("Ошибка валидации", ve.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Ошибка сохранения в БД", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) txtDeceasedName.getScene().getWindow();
        stage.close();
    }
}