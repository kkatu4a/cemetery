package com.katu4a.cladbise.model;
import java.util.UUID;

public record ComboItem(UUID id, String title) {
    @Override
    public String toString() {
        return title;
    }
}