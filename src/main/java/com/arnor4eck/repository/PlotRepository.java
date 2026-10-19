package com.arnor4eck.repository;

import com.arnor4eck.model.Plot;
import com.arnor4eck.util.enums.PlotStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PlotRepository extends AbstractJDBCRepository<Plot> {
    public PlotRepository(DataBase dataSource) {
        super(dataSource);
    }

    @Override
    protected Plot mapRow(ResultSet rs) throws SQLException {
        return new Plot(
                rs.getInt("id"),
                rs.getInt("sector_id"),
                rs.getInt("row_number"),
                rs.getInt("plot_number"),
                PlotStatus.fromString(rs.getString("status")),
                rs.getFloat("length_cm"),
                rs.getFloat("width_cm"),
                rs.getString("coordinates")
        );
    }
    @Override
    public Optional<Plot> get(int id) throws SQLException {
        String sql = "SELECT id, sector_id, row_number," +
                " plot_number, status, length_cm, " +
                "width_cm, coordinates" +
                " FROM plots WHERE id = ?";

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
    public Collection<Plot> getAll() throws SQLException {
        String sql = "SELECT id, sector_id, row_number," +
                " plot_number,  status, length_cm, " +
                "width_cm, coordinates " +
                "FROM plots";
        List<Plot> list = new ArrayList<>();
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
    public void save(Plot value) throws SQLException {
        String sql =
                "INSERT INTO plots(sector_id, row_number, " +
                        "plot_number, status, length_cm, " +
                "width_cm, coordinates) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, value.sectorId());
            stmt.setInt(2, value.rowNumber());
            stmt.setInt(3, value.plotNumber());
            stmt.setObject(4, value.status().toString(), Types.OTHER);
            stmt.setFloat(5, value.lengthCm());
            stmt.setFloat(6, value.widthCm());
            stmt.setString(7, value.coordinates());
            stmt.executeUpdate();
        }
    }
    @Override
    public void update(Plot value) throws SQLException {
        String sql =
                "UPDATE plots SET sector_id = ?, row_number = ?, " +
                        "plot_number = ?, status = ?, length_cm = ?, " +
                        "width_cm = ?, coordinates = ? " +
                        "WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, value.sectorId());
            stmt.setInt(2, value.rowNumber());
            stmt.setInt(3, value.plotNumber());
            stmt.setObject(4, value.status().toString(), Types.OTHER);
            stmt.setFloat(5, value.lengthCm());
            stmt.setFloat(6, value.widthCm());
            stmt.setString(7, value.coordinates());
            stmt.setInt(8, value.id());
            stmt.executeUpdate();
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM plots WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
