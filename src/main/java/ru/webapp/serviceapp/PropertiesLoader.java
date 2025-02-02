package ru.webapp.serviceapp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
    private static final String CONFIG = "database.properties";
    private Properties properties;

    public PropertiesLoader() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try(FileInputStream fis = new FileInputStream(CONFIG)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDataBaseUrl() {
        return properties.getProperty("db.url");
    }

    public String getDataBaseUserName() {
        return properties.getProperty("db.username");
    }

    public String getDataBasePassword() {
        return properties.getProperty("db.password");
    }

    public String getDataBaseDriver() {
        return properties.getProperty("db.driver");
    }
}
