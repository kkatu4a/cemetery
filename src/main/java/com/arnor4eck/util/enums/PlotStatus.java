package com.arnor4eck.util.enums;

public enum PlotStatus {
    FREE("Свободно"),
    RESERVED("Зарезервировано"),
    OCCUPIED("Занято");

    private final String value;

    public String getValue() {
        return value;
    }

    PlotStatus(String value) {
        this.value = value;
    }

    public static PlotStatus fromString(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Error with parse Plot");
        }
        for (PlotStatus plotStatus : PlotStatus.values()) {
            if (plotStatus.name().equalsIgnoreCase(str.trim())) {
                return plotStatus;
            }
        }
        throw new IllegalArgumentException("Найденного значения не найдено.");
    }

}
