package ru.webapp.serviceapp;

import com.google.gson.JsonArray;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DatabaseOperation {
    private static final PropertiesLoader propertiesLoader = new PropertiesLoader();

    public static Connection connection() throws SQLException {
        String dbUrl = propertiesLoader.getDataBaseUrl();
        String dbUsername = propertiesLoader.getDataBaseUserName();
        String dbPassword = propertiesLoader.getDataBasePassword();

        // Подключаемся к базе данных с использованием параметров из конфиг-файла
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    public static void insertPage(Connection conn, String title,
                                  String content, List<String> imageUrls) throws SQLException {
        String sql = "INSERT INTO pages VALUES(?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, title);
        ps.setString(2, content);

        ps.executeUpdate();
    }
}
