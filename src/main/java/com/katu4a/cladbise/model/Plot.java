package com.katu4a.cladbise.model;
import java.util.UUID;

import com.katu4a.cladbise.util.PlotStatus;
import org.jetbrains.annotations.Nullable;

public record Plot(
        UUID id,
        UUID sectorId,
        int rowNumber,
        int plotNumber,
        PlotStatus status,
        int lengthCm,
        int widthCm ,
        @Nullable String coordinates
) implements ExportModel {
    @Override
    public String toExportString() {
        return String.join("; ",
                String.valueOf(id),
                String.valueOf(sectorId),
                String.valueOf(rowNumber),
                String.valueOf(plotNumber),
                status.getValue(),
                String.valueOf(lengthCm),
                String.valueOf(widthCm),
                emptyIfNull(coordinates)
        );
    }
}

