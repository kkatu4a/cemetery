package com.arnor4eck.repository;

import com.arnor4eck.model.Request;
import com.arnor4eck.util.enums.RequestStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RequestRepository extends AbstractJDBCRepository<Request> {
    public RequestRepository(DataBase dataSource) {
        super(dataSource);
    }

    @Override
    protected Request mapRow(ResultSet rs) throws SQLException {
        return new Request(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getInt("employee_id"),
                rs.getInt("plot_id"),
                rs.getString("deceased_full_name"),
                rs.getObject("deceased_birth_date", LocalDate.class),
                rs.getObject("deceased_death_date", LocalDate.class),
                rs.getString("deceased_certificate"),
                RequestStatus.fromString(rs.getString("status")),
                rs.getString("total_cost"),
                rs.getString("notes"),
                rs.getObject("created_at", LocalDateTime.class)
        );
    }
    @Override
    public Optional<Request> get(int id) throws SQLException {
        String sql = "SELECT id, customer_id, employee_id," +
                " plot_id, deceased_full_name, " +
                "deceased_birth_date, deceased_death_date, deceased_certificate, " +
                "status, total_cost, notes, created_at" +
                " FROM requests WHERE id = ?";

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
    public Collection<Request> getAll() throws SQLException {
        String sql = "SELECT id, customer_id, employee_id," +
                " plot_id, deceased_full_name, " +
                "deceased_birth_date, deceased_death_date, deceased_certificate, " +
                "status, total_cost, notes, created_at " +
                "FROM requests";
        List<Request> list = new ArrayList<>();
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
    public void save(Request value) throws SQLException {
        String sql =
                "INSERT INTO requests(customer_id, employee_id, plot_id, " +
                        "deceased_full_name, deceased_birth_date, deceased_death_date, " +
                        "deceased_certificate, status, total_cost, notes, created_at) " +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, value.costumerId());
            if (value.employeeId() > 0) {
                stmt.setInt(2, value.employeeId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setInt(3, value.plotId());
            stmt.setString(4, value.deceasedFullName());
            stmt.setDate(5, value.deceasedBirthday() != null ? Date.valueOf(value.deceasedBirthday()) : null);
            stmt.setDate(6, value.deceasedDeathday() != null ? Date.valueOf(value.deceasedDeathday()) : null);
            stmt.setString(7, value.deceasedCertificate());
            stmt.setObject(8, value.status().toString(), Types.OTHER);

            BigDecimal cost = BigDecimal.ZERO;
            if (value.totalCost() != null && !value.totalCost().trim().isEmpty()) {
                try {
                    cost = new BigDecimal(value.totalCost().trim().replace(',', '.'));
                } catch (NumberFormatException ignored) {}
            }
            stmt.setBigDecimal(9, cost);
            stmt.setString(10, value.note());

            LocalDateTime created = value.createdAt() != null ? value.createdAt() : LocalDateTime.now();
            stmt.setTimestamp(11, Timestamp.valueOf(created));
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Request value) throws SQLException {
        String sql = """
        UPDATE requests SET 
            customer_id = ?, employee_id = ?, plot_id = ?, 
            deceased_full_name = ?, deceased_birth_date = ?, 
            deceased_death_date = ?, deceased_certificate = ?, 
            status = ?, total_cost = ?, notes = ?
        WHERE id = ?;
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value.costumerId());
            if (value.employeeId() > 0) {
                stmt.setInt(2, value.employeeId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setInt(3, value.plotId());
            stmt.setString(4, value.deceasedFullName());
            stmt.setDate(5, value.deceasedBirthday() != null ? Date.valueOf(value.deceasedBirthday()) : null);
            stmt.setDate(6, value.deceasedDeathday() != null ? Date.valueOf(value.deceasedDeathday()) : null);
            stmt.setString(7, value.deceasedCertificate());
            stmt.setObject(8, value.status().toString(), Types.OTHER);

            BigDecimal cost = BigDecimal.ZERO;
            if (value.totalCost() != null && !value.totalCost().trim().isEmpty()) {
                try {
                    cost = new BigDecimal(value.totalCost().trim().replace(',', '.'));
                } catch (NumberFormatException ignored) {}
            }
            stmt.setBigDecimal(9, cost);
            stmt.setString(10, value.note());

            stmt.setInt(11, value.id());

            stmt.executeUpdate();
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM requests WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
