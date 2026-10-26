package com.katu4a.cladbise.model;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public record Customer(
        UUID id,
        String fullName,
        String phone,
        @Nullable String email,
        LocalDateTime createdAt
) implements ExportModel {
    @Override
    public String toExportString() {
        return String.join("; ",
                String.valueOf(id),
                fullName,
                phone,
                emptyIfNull(email),
                createdAt.toString()
        );
    }
}

