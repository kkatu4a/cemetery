package com.arnor4eck.util.enums;

public enum Role {
    ADMIN,
    MANAGER,
    WORKER;
    public static Role fromString(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Error with parse role");
        }
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(str.trim())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Error with parse role");
    }
}


