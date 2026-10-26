package com.katu4a.cladbise.util;

import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.util.StringConverter;

import java.util.function.Function;

public final class FormUtils {

    private static final String INVALID_STYLE = "invalid";

    private FormUtils() {
    }

    public static void markInvalid(Node node) {
        if (node != null && !node.getStyleClass().contains(INVALID_STYLE)) {
            node.getStyleClass().add(INVALID_STYLE);
        }
    }

    public static void clearInvalid(Node... nodes) {
        for (Node node : nodes) {
            node.getStyleClass().remove(INVALID_STYLE);
        }
    }

    public static String text(TextInputControl field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    public static <T> StringConverter<T> converter(Function<T, String> toText) {
        return new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : toText.apply(item);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }
}