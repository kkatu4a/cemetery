package com.katu4a.cladbise.model;
import java.util.UUID;

import java.time.LocalDateTime;

public record Sector(
        UUID id,
        String name,
        LocalDateTime createdAt,
        boolean archived
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

