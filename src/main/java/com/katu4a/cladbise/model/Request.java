package com.katu4a.cladbise.model;
import java.util.UUID;

import com.katu4a.cladbise.util.RequestStatus;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Request(
        UUID id,
        UUID costumerId,
        UUID employeeId,
        UUID plotId,
        @Nullable String deceasedFullName,
        @Nullable LocalDate deceasedBirthday,
        @Nullable LocalDate deceasedDeathday,
        @Nullable String deceasedCertificate,
        RequestStatus status,
        String totalCost,
        @Nullable String note,
        LocalDateTime createdAt
) implements ExportModel {
    @Override
    public String toExportString() {
        return String.join("; ",
                String.valueOf(this.id()),
                String.valueOf(this.costumerId()),
                String.valueOf(this.employeeId()),
                String.valueOf(this.plotId()),
                emptyIfNull(this.deceasedFullName()),
                emptyIfNull(this.deceasedBirthday()),
                emptyIfNull(this.deceasedDeathday()),
                emptyIfNull(this.deceasedCertificate()),
                this.status().getValue(),
                this.totalCost(),
                emptyIfNull(this.note()),
                this.createdAt().toString()
        );
    }
}
