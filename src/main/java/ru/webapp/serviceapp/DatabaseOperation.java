package ru.webapp.serviceapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DatabaseOperation {
    private static final PropertiesLoader propertiesLoader = new PropertiesLoader();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Connection connection() throws SQLException {
        String dbUrl = propertiesLoader.getDataBaseUrl();
        String dbUsername = propertiesLoader.getDataBaseUserName();
        String dbPassword = propertiesLoader.getDataBasePassword();

        // Подключаемся к базе данных с использованием параметров из конфиг-файла
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    public static void insertPage(Connection conn, String title,
                                  String content, List<String> imageUrls) throws SQLException {
        String imageUrlsJson;
        try{
            imageUrlsJson = objectMapper.writeValueAsString(imageUrls);
        }
        catch(JsonProcessingException e){
            throw new SQLException(e);
        }

        String sql = "INSERT INTO pages VALUES(?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, title);
        ps.setString(2, content);
        ps.setString(3, imageUrlsJson);

        ps.executeUpdate();
    }
}
