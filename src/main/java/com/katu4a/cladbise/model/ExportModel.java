package com.katu4a.cladbise.model;

public interface ExportModel {
    String toExportString();

    default String emptyIfNull(Object object) {
        return object == null ? "" : object.toString();
    }
}
