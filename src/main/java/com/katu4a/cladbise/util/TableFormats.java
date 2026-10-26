package com.katu4a.cladbise.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TableFormats {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private TableFormats() {
    }

    public static <T> Callback<TableColumn<T, LocalDateTime>, TableCell<T, LocalDateTime>> dateTimeCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DATE_TIME.format(item));
            }
        };
    }
}