package com.arnor4eck.repository;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public interface Repository<T> {
    Optional<T> get(int id) throws SQLException;
    Collection<T> getAll() throws SQLException;
    void save(T value) throws SQLException;
    void delete(int id) throws SQLException;
    void update(T value) throws SQLException;
}
