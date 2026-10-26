package com.katu4a.cladbise.model;
import java.util.UUID;

import com.katu4a.cladbise.util.Role;

import java.time.LocalDateTime;

public record Employee (
        UUID id,
        String fullName,
        Role role,
        String login,
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
                String.valueOf(isActive),
                createdAt.toString()
        );
    }
}

