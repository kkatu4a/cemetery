package com.arnor4eck.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    private final DataSource dataSource;


    public DataBase() {
        try {
            this.dataSource = initDataSource();
            initDb();
        } catch (SQLException | IOException e) {
            System.out.printf("Не удалось присоединиться к базе данных: %s", e.getMessage());
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

    private DataSource initDataSource() {
        Dotenv dotenv = Dotenv.load();

        PGSimpleDataSource source = new PGSimpleDataSource();

        String url = String.format("jdbc:postgresql://%s:%s/%s",
                dotenv.get("DB_HOST"),
                dotenv.get("DB_PORT"),
                dotenv.get("DB_NAME")
        );

        source.setUrl(url);
        source.setUser(dotenv.get("DB_USER"));
        source.setPassword(dotenv.get("DB_PASSWORD"));

        return source;
    }

    private void initDb() throws SQLException, IOException {
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