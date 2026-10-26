package com.katu4a.cladbise.controller;
import com.katu4a.cladbise.util.FxAsync;

import com.katu4a.cladbise.model.Customer;
import com.katu4a.cladbise.service.CustomerService;
import com.katu4a.cladbise.service.CustomerValidator;
import com.katu4a.cladbise.service.ValidationException;
import com.katu4a.cladbise.util.AlertHelper;
import com.katu4a.cladbise.util.FormUtils;
import com.katu4a.cladbise.util.SavableForm;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.Map;

public class CustomerFormController implements SavableForm {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private Label errorLabel;

    private CustomerService customerService;
    private Customer editing;
    private boolean saved;
    private Map<String, Node> fields;

    public void init(CustomerService customerService, Customer editing) {
        this.customerService = customerService;
        this.editing = editing;
        this.fields = Map.of(
                "fullName", fullNameField,
                "phone", phoneField,
                "email", emailField);

        titleLabel.setText(editing == null ? "Новый заявитель" : "Редактирование заявителя");

        if (editing != null) {
            fullNameField.setText(editing.fullName());
            phoneField.setText(editing.phone());
            emailField.setText(editing.email() == null ? "" : editing.email());
        }
    }

    @Override
    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSave() {
        FormUtils.clearInvalid(fullNameField, phoneField, emailField);
        errorLabel.setText("");

        String email = FormUtils.text(emailField);
        Customer input = new Customer(
                editing == null ? null : editing.id(),
                FormUtils.text(fullNameField),
                FormUtils.text(phoneField),
                email.isEmpty() ? null : email,
                editing == null ? LocalDateTime.now() : editing.createdAt());

        final Customer customer;
        try {
            customer = CustomerValidator.validate(input);
        } catch (ValidationException error) {
            Node field = fields.get(error.getField());
            FormUtils.markInvalid(field);
            if (field != null) field.requestFocus();
            errorLabel.setText(error.getMessage());
            return;
        }

        FxAsync.run(titleLabel, () -> {
            if (editing == null) return customerService.create(customer);
            return customerService.update(customer);
        }, result -> { saved = true; closeWindow(); }, error -> {
            if (error instanceof ValidationException validation && validation.getField() != null)
                FormUtils.markInvalid(fields.get(validation.getField()));
            AlertHelper.showError(titleLabel.getScene().getWindow(), error.getMessage());
        });
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}
