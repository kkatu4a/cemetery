package com.arnor4eck.repository;

import com.arnor4eck.model.Sector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SectorRepository extends AbstractJDBCRepository<Sector> {
    public SectorRepository(DataBase dataSource) {
        super(dataSource);
    }

    @Override
    protected Sector mapRow(ResultSet rs) throws SQLException {
        return new Sector(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getObject("created_at", LocalDateTime.class)
        );
    }

    @Override
    public Optional<Sector> get(int id) throws SQLException {
        String sql = "SELECT id, name, created_at FROM sectors WHERE id = ?";

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
    public Collection<Sector> getAll() throws SQLException {
        String sql = "SELECT id, name, created_at FROM sectors";

        List<Sector> list = new ArrayList<>();
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
    public void save(Sector value) throws SQLException {
        String sql =
                "INSERT INTO sectors(name, created_at)" +
                        " VALUES(?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, value.name());
            stmt.setTimestamp(2, Timestamp.valueOf(value.createdAt()));
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Sector value) throws SQLException {
        String sql =
                "UPDATE sectors SET name = ?, created_at = ? " +
                        "WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, value.name());
            stmt.setTimestamp(2, Timestamp.valueOf(value.createdAt()));
            stmt.setInt(3, value.id());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM sectors WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
