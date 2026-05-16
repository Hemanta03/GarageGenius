package com.garagegenius.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralized JDBC connection factory for the application.
 *
 * <p>Reads {@code db.properties} from the classpath and initializes the JDBC driver once
 * on class load. Callers obtain a new {@link Connection} via {@link #getConnection()} and
 * should close it after use (typically in DAO {@code finally} blocks).</p>
 */
public class DBConnection {

    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DRIVER;

    static {
        try {
            InputStream input = DBConnection.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties");

            Properties props = new Properties();
            props.load(input);

            DRIVER   = props.getProperty("db.driver");
            URL      = props.getProperty("db.url");
            USERNAME = props.getProperty("db.username");
            PASSWORD = props.getProperty("db.password");

            Class.forName(DRIVER);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load database configuration: " + e.getMessage());
        }
    }

    /**
     * Creates a new JDBC connection using properties from {@code db.properties}.
     *
     * @return an open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Safely closes a JDBC connection, ignoring nulls.
     *
     * @param conn connection to close (may be {@code null})
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}