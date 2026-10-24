package com.arnor4eck.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    private final DataSource dataSource;

    public DataBase() {
        try {
            this.dataSource = initDataSource();
            initDb();
        } catch (SQLException | IOException e) {
            System.err.printf("Не удалось присоединиться к базе данных: %s%n", e.getMessage());
            throw new RuntimeException("Ошибка подключения к базе данных: " + e.getMessage(), e);
        }
    }

    private DataSource initDataSource() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String host = dotenv.get("DB_HOST");
        if (host == null || host.isBlank()) host = System.getenv().getOrDefault("DB_HOST", "localhost");

        String port = dotenv.get("DB_PORT");
        if (port == null || port.isBlank()) port = System.getenv().getOrDefault("DB_PORT", "5433");

        String name = dotenv.get("DB_NAME");
        if (name == null || name.isBlank()) name = System.getenv().getOrDefault("DB_NAME", "database_csp");

        String user = dotenv.get("DB_USER");
        if (user == null || user.isBlank()) user = System.getenv().getOrDefault("DB_USER", "admin");

        String pass = dotenv.get("DB_PASSWORD");
        if (pass == null) pass = System.getenv().getOrDefault("DB_PASSWORD", "password");

        PGSimpleDataSource source = new PGSimpleDataSource();
        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);

        source.setUrl(url);
        source.setUser(user);
        source.setPassword(pass);

        return source;
    }

    private void initDb() throws SQLException, IOException {
        try (Connection connection = this.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "requests", new String[]{"TABLE"})) {
                if (rs.next()) {
                    // База данных уже инициализирована, не сбрасываем существующие данные
                    return;
                }
            }
        }

        try (InputStream in = DataBase.class.getResourceAsStream("/init.sql")) {
            if (in == null) {
                throw new IOException("Файл ресурса /init.sql не найден");
            }

            String prepareDB = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            String[] scripts = prepareDB.split(";");

            try (Connection connection = this.getConnection();
                 Statement statement = connection.createStatement()) {

                for (String s : scripts) {
                    if (!s.trim().isEmpty()) {
                        statement.execute(s);
                    }
                }
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}