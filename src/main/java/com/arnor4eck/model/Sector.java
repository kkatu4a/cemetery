package com.arnor4eck.model;

import java.time.LocalDateTime;

public record Sector(
        int id,
        String name,
        LocalDateTime createdAt
) implements ExportModel {
    @Override
    public String toExportString() {
        return String.join("; ",
                String.valueOf(id),
                name,
                createdAt.toString()
        );
    }
}
