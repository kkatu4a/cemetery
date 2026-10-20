package com.arnor4eck.model;

import com.arnor4eck.model.ExportModel;
import com.arnor4eck.util.enums.Role;

import java.time.LocalDateTime;

public record Employee (
        int id,
        String fullName,
        Role role,
        String login,
        String passwordHash,
        boolean isActive,
        LocalDateTime createdAt
) implements ExportModel {
    @Override
    public String toExportString() {
        return String.join("; ",
                String.valueOf(this.id()),
                fullName,
                role.name(),
                login,
                passwordHash,
                String.valueOf(isActive),
                createdAt.toString()
        );
    }
}
