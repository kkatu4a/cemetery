package com.arnor4eck.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractJDBCRepository<T> implements Repository<T> {
    protected final DataBase dataBase;

    public AbstractJDBCRepository(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    protected Connection getConnection() throws SQLException{
        return dataBase.getConnection();
    }

    protected abstract T mapRow(ResultSet rs) throws SQLException;
}
