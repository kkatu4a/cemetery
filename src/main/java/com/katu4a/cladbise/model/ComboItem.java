package com.katu4a.cladbise.model;

public record ComboItem(int id, String title) {
    @Override
    public String toString() {
        return title;
    }
}