package com.katu4a.cladbise.model;

import com.katu4a.cladbise.util.PlotStatus;
import org.jetbrains.annotations.Nullable;

public record Plot(
        int id,
        int sectorId,
        int rowNumber,
        int plotNumber,
        PlotStatus status,
        float lengthCm,
        float widthCm ,
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

