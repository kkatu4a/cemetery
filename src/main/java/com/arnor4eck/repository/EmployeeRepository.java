package com.arnor4eck.repository;

import com.arnor4eck.model.Employee;
import com.arnor4eck.util.enums.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository extends AbstractJDBCRepository<Employee> {

    public EmployeeRepository(DataBase dataSource) {
        super(dataSource);
    }

    @Override
    protected Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getString("full_name"),
                Role.fromString(rs.getString("role")),
                rs.getString("login"),
                rs.getString("password_hash"),
                rs.getBoolean("is_active"),
                rs.getObject("created_at", LocalDateTime.class)
        );
    }
    @Override
    public Optional<Employee> get(int id) throws SQLException {
        String sql = "SELECT id, full_name, role, login," +
                " password_hash, is_active, created_at" +
                " FROM employees WHERE id = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()){
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    @Override
    public Collection<Employee> getAll() throws SQLException {
        String sql = "SELECT id, full_name, role, login," +
                " password_hash, is_active, created_at" +
                " FROM employees";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public void save(Employee value) throws SQLException {
        String sql = "INSERT INTO employees(full_name, role, login, password_hash, " +
                "is_active, created_at) VALUES(?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, value.fullName());
            stmt.setObject(2, value.role().toString(), Types.OTHER);
            stmt.setString(3, value.login());
            stmt.setString(4, value.passwordHash());
            stmt.setBoolean(5, value.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(value.createdAt()));
            stmt.executeUpdate();
        }
    }
    @Override
    public void update(Employee value) throws SQLException {
        String sql = "UPDATE employees SET full_name = ?, role = ?, login = ?, " +
                "password_hash = ?, is_active = ?, created_at = ? " +
                "WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, value.fullName());
            stmt.setObject(2, value.role().toString(), Types.OTHER);
            stmt.setString(3, value.login());
            stmt.setString(4, value.passwordHash());
            stmt.setBoolean(5, value.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(value.createdAt()));
            stmt.setInt(7, value.id());
            stmt.executeUpdate();
        }
    }


    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
