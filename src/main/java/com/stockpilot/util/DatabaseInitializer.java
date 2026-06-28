package com.stockpilot.util;

import com.stockpilot.exception.DataAccessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Reads and executes schema.sql at application startup to ensure all tables exist.
 * Uses IF NOT EXISTS / DROP TABLE IF EXISTS patterns in schema.sql, so it's safe to run on every launch.
 */
public class DatabaseInitializer {

    public static void initialize() {
        String sql = loadSchema();
        executeSchema(sql);
    }

    private static String loadSchema() {
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                throw new DataAccessException("schema.sql not found in classpath resources");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to load schema.sql", e);
        }
    }

    private static void executeSchema(String fullSql) {
        // Split on ";" to get individual statements
        String[] statements = fullSql.split(";");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                // Strip comment lines (lines starting with --) from the statement
                String cleaned = stripComments(sql).trim();
                if (!cleaned.isEmpty()) {
                    stmt.execute(cleaned);
                }
            }
            System.out.println("[DatabaseInitializer] Schema initialized successfully.");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute schema.sql", e);
        }
    }

    /**
     * Removes lines that start with "--" (SQL single-line comments)
     * while preserving the actual SQL content.
     */
    private static String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\\n")) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("--")) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
