package com.katu4a.cladbise.controller;

import com.katu4a.cladbise.model.Employee;
import com.katu4a.cladbise.service.EmployeeService;
import com.katu4a.cladbise.service.ValidationException;
import com.katu4a.cladbise.util.AlertHelper;
import com.katu4a.cladbise.util.FormUtils;
import com.katu4a.cladbise.util.Role;
import com.katu4a.cladbise.util.SavableForm;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.Map;

public class EmployeeFormController implements SavableForm {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private ComboBox<Role> roleCombo;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox activeCheck;

    private EmployeeService employeeService;
    private Employee editing;
    private boolean saved;
    private Map<String, Node> fields;

    public void init(EmployeeService employeeService, Employee editing) {
        this.employeeService = employeeService;
        this.editing = editing;
        this.fields = Map.of(
                "fullName", fullNameField,
                "role", roleCombo,
                "login", loginField,
                "password", passwordField);

        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));

        if (editing == null) {
            titleLabel.setText("Новый сотрудник");
            roleCombo.setValue(Role.MANAGER);
            activeCheck.setSelected(true);
        } else {
            titleLabel.setText("Редактирование сотрудника");
            fullNameField.setText(editing.fullName());
            roleCombo.setValue(editing.role());
            loginField.setText(editing.login());
            passwordField.setPromptText("Оставьте пустым, чтобы не менять");
            activeCheck.setSelected(editing.isActive());
        }
    }

    @Override
    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSave() {
        FormUtils.clearInvalid(fullNameField, roleCombo, loginField, passwordField);

        Employee employee = new Employee(
                editing == null ? 0 : editing.id(),
                FormUtils.text(fullNameField),
                roleCombo.getValue(),
                FormUtils.text(loginField),
                editing == null ? "" : editing.passwordHash(),
                activeCheck.isSelected(),
                editing == null ? LocalDateTime.now() : editing.createdAt());
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        try {
            if (editing == null) {
                employeeService.create(employee, password);
            } else {
                employeeService.update(employee, password);
            }
            saved = true;
            closeWindow();
        } catch (ValidationException e) {
            FormUtils.markInvalid(fields.get(e.getField()));
            AlertHelper.showError(titleLabel.getScene().getWindow(), e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError(titleLabel.getScene().getWindow(),
                    "Не удалось сохранить сотрудника: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}