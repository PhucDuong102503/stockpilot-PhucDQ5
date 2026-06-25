package com.stockpilot.util;

import com.stockpilot.exception.DataAccessException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find db.properties");
            }
            properties.load(input);
            // Load driver class dynamically
            String driver = properties.getProperty("db.driver");
            if (driver != null) {
                Class.forName(driver);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing database properties or driver", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
            );
        } catch (SQLException e) {
            throw new DataAccessException("Failed to connect to the database", e);
        }
    }

    // A quick verification test method
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connection successful! Database product name: " + conn.getMetaData().getDatabaseProductName());
            }
        } catch (Exception e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }
}
