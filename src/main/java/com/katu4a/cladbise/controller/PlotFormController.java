package com.katu4a.cladbise.controller;

import com.katu4a.cladbise.model.Plot;
import com.katu4a.cladbise.model.Sector;
import com.katu4a.cladbise.service.PlotService;
import com.katu4a.cladbise.service.ValidationException;
import com.katu4a.cladbise.util.AlertHelper;
import com.katu4a.cladbise.util.PlotStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.List;

public class PlotFormController {

    private static final String INVALID_STYLE = "invalid";

    @FXML
    private Label titleLabel;
    @FXML
    private ComboBox<Sector> sectorCombo;
    @FXML
    private TextField rowField;
    @FXML
    private TextField plotNumberField;
    @FXML
    private ComboBox<PlotStatus> statusCombo;
    @FXML
    private TextField lengthField;
    @FXML
    private TextField widthField;
    @FXML
    private TextField coordinatesField;

    private PlotService plotService;
    private Plot editing;
    private boolean saved;

    public void init(PlotService plotService, Plot editing) {
        this.plotService = plotService;
        this.editing = editing;

        titleLabel.setText(editing == null ? "Новое место" : "Редактирование места");

        sectorCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sector sector) {
                return sector == null ? "" : sector.name();
            }

            @Override
            public Sector fromString(String string) {
                return null;
            }
        });

        statusCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(PlotStatus status) {
                return status == null ? "" : status.getValue();
            }

            @Override
            public PlotStatus fromString(String string) {
                return null;
            }
        });
        statusCombo.setItems(FXCollections.observableArrayList(PlotStatus.values()));

        try {
            sectorCombo.setItems(FXCollections.observableArrayList(plotService.findAllSectors()));
        } catch (Exception e) {
            showError("Не удалось загрузить секторы: " + e.getMessage());
        }

        if (editing == null) {
            statusCombo.setValue(PlotStatus.FREE);
            lengthField.setText("200");
            widthField.setText("100");
        } else {
            fillForm(editing);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSave() {
        clearErrors();
        try {
            Plot plot = readForm();
            if (editing == null) {
                plotService.create(plot);
            } else {
                plotService.update(plot);
            }
            saved = true;
            closeWindow();
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Не удалось сохранить место: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void fillForm(Plot plot) {
        sectorCombo.getItems().stream()
                .filter(sector -> sector.id() == plot.sectorId())
                .findFirst()
                .ifPresent(sectorCombo::setValue);
        rowField.setText(String.valueOf(plot.rowNumber()));
        plotNumberField.setText(String.valueOf(plot.plotNumber()));
        statusCombo.setValue(plot.status());
        lengthField.setText(String.valueOf((int) plot.lengthCm()));
        widthField.setText(String.valueOf((int) plot.widthCm()));
        coordinatesField.setText(plot.coordinates() == null ? "" : plot.coordinates());
    }

    private Plot readForm() {
        Sector sector = sectorCombo.getValue();
        if (sector == null) {
            markInvalid(sectorCombo);
            throw new ValidationException("Сектор: выберите сектор из списка");
        }
        int row = readPositiveInt(rowField, "Ряд");
        int plotNumber = readPositiveInt(plotNumberField, "Номер места");

        PlotStatus status = statusCombo.getValue();
        if (status == null) {
            markInvalid(statusCombo);
            throw new ValidationException("Статус: выберите статус из списка");
        }
        int length = readPositiveInt(lengthField, "Длина");
        int width = readPositiveInt(widthField, "Ширина");

        String coordinates = coordinatesField.getText() == null ? "" : coordinatesField.getText().trim();
        if (coordinates.length() > 100) {
            markInvalid(coordinatesField);
            throw new ValidationException("Координаты: не длиннее 100 символов");
        }

        return new Plot(
                editing == null ? 0 : editing.id(),
                sector.id(),
                row,
                plotNumber,
                status,
                length,
                width,
                coordinates.isEmpty() ? null : coordinates
        );
    }

    private int readPositiveInt(TextField field, String name) {
        String text = field.getText() == null ? "" : field.getText().trim();
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            markInvalid(field);
            throw new ValidationException(name + ": введите целое число больше 0");
        }
    }

    private void markInvalid(Node node) {
        if (!node.getStyleClass().contains(INVALID_STYLE)) {
            node.getStyleClass().add(INVALID_STYLE);
        }
    }

    private void clearErrors() {
        List<Node> fields = List.of(sectorCombo, rowField, plotNumberField,
                statusCombo, lengthField, widthField, coordinatesField);
        for (Node field : fields) {
            field.getStyleClass().remove(INVALID_STYLE);
        }
    }

    private void showError(String message) {
        Window owner = titleLabel.getScene() == null ? null : titleLabel.getScene().getWindow();
        AlertHelper.showError(owner, message);
    }

    private void closeWindow() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}