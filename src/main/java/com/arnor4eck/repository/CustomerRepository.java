package com.arnor4eck.repository;

import com.arnor4eck.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CustomerRepository extends AbstractJDBCRepository<Customer> {
    public CustomerRepository(DataBase dataSource) {
        super(dataSource);
    }

    @Override
    protected Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getObject("created_at", LocalDateTime.class)
        );
    }
    @Override
    public Optional<Customer> get(int id) throws SQLException {
        String sql = "SELECT id, full_name, phone," +
                " email, created_at" +
                " FROM customers WHERE id = ?";
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
    public Collection<Customer> getAll() throws SQLException {
        String sql = "SELECT id, full_name, phone," +
                "email, created_at " +
                "FROM customers";
        List<Customer> list = new ArrayList<>();
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
    public void save(Customer value) throws SQLException {
        String sql =
                "INSERT INTO customers(full_name, phone, email, created_at)" +
                        " VALUES(?, ?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, value.fullName());
            stmt.setString(2, value.phone());
            stmt.setString(3, value.email());
            stmt.setTimestamp(4, Timestamp.valueOf(value.createdAt()));
            stmt.executeUpdate();
        }
    }
    @Override
    public void update(Customer value) throws SQLException {
        String sql =
                "UPDATE customers SET full_name = ?, phone = ?, email = ?, created_at = ? " +
                        "WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, value.fullName());
            stmt.setString(2, value.phone());
            stmt.setString(3, value.email());
            stmt.setTimestamp(4, Timestamp.valueOf(value.createdAt()));
            stmt.setInt(5, value.id());
            stmt.executeUpdate();
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }

    }


}
