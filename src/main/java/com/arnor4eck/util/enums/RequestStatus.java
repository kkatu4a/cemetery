package com.arnor4eck.util.enums;

public enum RequestStatus {
    NEW("Новый"),
    PROCESSING("В обработке"),
    APPROVED("Одобренный"),
    REJECTED("Отказанный"),
    COMPLETED("Выполненный");

    private final String value;

    RequestStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RequestStatus fromString(String str){
        if (str == null) {
            throw new IllegalArgumentException("Error with parse RequestStatus");
        }
        for (RequestStatus requestStatus : RequestStatus.values()) {
            if (requestStatus.name().equalsIgnoreCase(str.trim())) {
                return requestStatus;
            }
        }
        throw new IllegalArgumentException("Error with parse RequestStatus");
    }


}
