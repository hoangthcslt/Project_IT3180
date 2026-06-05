package com.bluemoon.services;

import com.bluemoon.utils.DBConnection;
import org.junit.jupiter.api.Test;
import java.sql.*;

public class DbInspectorTest {

    @Test
    public void inspectDb() {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("--- Connection Successful ---");
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Print tables
            System.out.println("--- TABLES ---");
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    System.out.println(rs.getString("TABLE_NAME"));
                }
            }
            
            // Describe users table
            describeTable(conn, "users");
            describeTable(conn, "user_group");
            describeTable(conn, "user_group_mapping");
            describeTable(conn, "nop_tien");
            describeTable(conn, "hoa_don");
            
            // Show data from user_group
            System.out.println("--- user_group data ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM user_group")) {
                while (rs.next()) {
                    System.out.println("id: " + rs.getInt("id") + ", ten_nhom: " + rs.getString("ten_nhom") + ", mo_ta: " + rs.getString("mo_ta"));
                }
            }
            
            // Show data from users
            System.out.println("--- users data ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                while (rs.next()) {
                    System.out.println("id: " + rs.getInt("id") + ", username: " + rs.getString("username") + ", role: " + rs.getString("role"));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void describeTable(Connection conn, String tableName) {
        System.out.println("--- DESCRIBE TABLE: " + tableName + " ---");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE `" + tableName + "`")) {
            while (rs.next()) {
                System.out.println(rs.getString("Field") + " | " + rs.getString("Type") + " | " + rs.getString("Null") + " | " + rs.getString("Key") + " | " + rs.getString("Default"));
            }
        } catch (Exception e) {
            System.out.println("Error describing table " + tableName + ": " + e.getMessage());
        }
    }
}
