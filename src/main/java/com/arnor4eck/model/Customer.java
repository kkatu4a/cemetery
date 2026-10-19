package com.arnor4eck.model;

import com.arnor4eck.model.ExportModel;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public record Customer(
        int id,
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
