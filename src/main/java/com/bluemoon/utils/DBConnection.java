package com.bluemoon.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bluemoon_db";
    private static final String USER = "root"; // Nhắc team tự đổi pass ở máy local
    private static final String PASSWORD = "Longkongu@123";
    private static Connection connection = null;

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                // If password connection fails, try with empty password as a fallback
                try {
                    connection = DriverManager.getConnection(URL, USER, "");
                } catch (SQLException ex) {
                    throw e; // throw original if fallback fails too
                }
            }
        }
        return connection;
    }
}