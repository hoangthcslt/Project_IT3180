package com.bluemoon.repositories;

import com.bluemoon.models.Permission;
import com.bluemoon.models.User;
import com.bluemoon.models.UserGroup;
import com.bluemoon.models.UserGroupAssignment;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionRepository {
    private static final Permission[] SYSTEM_PERMISSIONS = {
            new Permission("TRANG_CHU", "Trang chủ"),
            new Permission("HO_KHAU", "Hộ khẩu"),
            new Permission("NHAN_KHAU", "Nhân khẩu"),
            new Permission("KHOAN_THU", "Khoản thu"),
            new Permission("NOP_TIEN", "Nộp tiền"),
            new Permission("THONG_KE", "Thống kê"),
            new Permission("PHAN_QUYEN", "Phân quyền"),
            new Permission("TIEN_ICH", "Tiện ích"),
            new Permission("PHAN_ANH_GUI", "Gửi phản ánh"),
            new Permission("PHAN_ANH_TIEP_NHAN", "Tiếp nhận phản ánh")
    };

    public PermissionRepository() {
        ensureTables();
        seedPermissions();
        ensureDefaultGroup();
    }

    public List<UserGroup> findGroups(String keyword) {
        List<UserGroup> groups = new ArrayList<>();
        String sql = "SELECT * FROM user_group WHERE (? IS NULL OR ten_nhom LIKE ?) ORDER BY id DESC";
        String value = keyword == null || keyword.trim().isEmpty() ? null : "%" + keyword.trim() + "%";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setString(2, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    groups.add(new UserGroup(rs.getInt("id"), rs.getString("ten_nhom"), rs.getString("mo_ta")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public boolean insertGroup(UserGroup group) {
        String sql = "INSERT INTO user_group (ten_nhom, mo_ta) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, group.getTenNhom());
            pstmt.setString(2, group.getMoTa());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateGroup(UserGroup group) {
        String sql = "UPDATE user_group SET ten_nhom = ?, mo_ta = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, group.getTenNhom());
            pstmt.setString(2, group.getMoTa());
            pstmt.setInt(3, group.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteGroup(int groupId) {
        String sql = "DELETE FROM user_group WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Permission> getSystemPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT code, name FROM permissions ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                permissions.add(new Permission(rs.getString("code"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public Set<String> getPermissionCodesByGroup(int groupId) {
        Set<String> codes = new HashSet<>();
        String sql = "SELECT permission_code FROM group_permission WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    codes.add(rs.getString("permission_code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return codes;
    }

    public Set<String> getPermissionCodesByUser(int userId) {
        Set<String> codes = new HashSet<>();
        String sql = "SELECT DISTINCT gp.permission_code " +
                "FROM user_group_mapping ugm " +
                "JOIN group_permission gp ON ugm.group_id = gp.group_id " +
                "WHERE ugm.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    codes.add(rs.getString("permission_code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return codes;
    }

    public boolean hasUserGroup(int userId) {
        String sql = "SELECT COUNT(*) FROM user_group_mapping WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveGroupPermissions(int groupId, Set<String> permissionCodes) {
        String deleteSql = "DELETE FROM group_permission WHERE group_id = ?";
        String insertSql = "INSERT INTO group_permission (group_id, permission_code) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                deleteStmt.setInt(1, groupId);
                deleteStmt.executeUpdate();
                for (String code : permissionCodes) {
                    insertStmt.setInt(1, groupId);
                    insertStmt.setString(2, code);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<UserGroupAssignment> findUserAssignments(String keyword) {
        List<UserGroupAssignment> users = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.role, COALESCE(MIN(g.id), 0) AS group_id, " +
                "COALESCE(GROUP_CONCAT(g.ten_nhom SEPARATOR ', '), 'Trống') AS group_name " +
                "FROM users u " +
                "LEFT JOIN user_group_mapping ugm ON u.id = ugm.user_id " +
                "LEFT JOIN user_group g ON ugm.group_id = g.id " +
                "WHERE (? IS NULL OR u.username LIKE ? OR u.role LIKE ?) " +
                "GROUP BY u.id, u.username, u.role " +
                "ORDER BY u.id DESC";
        String value = keyword == null || keyword.trim().isEmpty() ? null : "%" + keyword.trim() + "%";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setString(2, value);
            pstmt.setString(3, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new UserGroupAssignment(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getInt("group_id"),
                            rs.getString("group_name"),
                            rs.getString("role")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void assignUserToGroup(int userId, int groupId) {
        String sql = "INSERT IGNORE INTO user_group_mapping (user_id, group_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserAndGroup(int userId, String username, String role, int groupId) {
        String updateUserSql = "UPDATE users SET username = ?, role = ? WHERE id = ?";
        String deleteGroupsSql = "DELETE FROM user_group_mapping WHERE user_id = ?";
        String insertGroupSql = "INSERT INTO user_group_mapping (user_id, group_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement updateUserStmt = conn.prepareStatement(updateUserSql);
                    PreparedStatement deleteGroupsStmt = conn.prepareStatement(deleteGroupsSql);
                    PreparedStatement insertGroupStmt = conn.prepareStatement(insertGroupSql)) {
                updateUserStmt.setString(1, username);
                updateUserStmt.setString(2, role);
                updateUserStmt.setInt(3, userId);
                updateUserStmt.executeUpdate();

                deleteGroupsStmt.setInt(1, userId);
                deleteGroupsStmt.executeUpdate();

                insertGroupStmt.setInt(1, userId);
                insertGroupStmt.setInt(2, groupId);
                insertGroupStmt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the cap nhat user/group.", e);
        }
    }

    public void insertUserAndGroup(String username, String password, String role, int groupId) {
        String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        String insertGroupSql = "INSERT INTO user_group_mapping (user_id, group_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insertUserStmt = conn.prepareStatement(insertUserSql,
                    Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement insertGroupStmt = conn.prepareStatement(insertGroupSql)) {

                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password);
                insertUserStmt.setString(3, role);
                insertUserStmt.executeUpdate();

                int userId = -1;
                try (ResultSet rs = insertUserStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }

                if (userId != -1) {
                    insertGroupStmt.setInt(1, userId);
                    insertGroupStmt.setInt(2, groupId);
                    insertGroupStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the them user.", e);
        }
    }

    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureTables() {
        String[] sqlStatements = {
                "CREATE TABLE IF NOT EXISTS user_group (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "ten_nhom VARCHAR(100) NOT NULL UNIQUE, " +
                        "mo_ta VARCHAR(255) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
                "CREATE TABLE IF NOT EXISTS permissions (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "code VARCHAR(50) NOT NULL UNIQUE, " +
                        "name VARCHAR(100) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
                "CREATE TABLE IF NOT EXISTS group_permission (" +
                        "group_id INT NOT NULL, " +
                        "permission_code VARCHAR(50) NOT NULL, " +
                        "PRIMARY KEY (group_id, permission_code), " +
                        "FOREIGN KEY (group_id) REFERENCES user_group(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (permission_code) REFERENCES permissions(code) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
                "CREATE TABLE IF NOT EXISTS user_group_mapping (" +
                        "user_id INT NOT NULL, " +
                        "group_id INT NOT NULL, " +
                        "PRIMARY KEY (user_id, group_id), " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (group_id) REFERENCES user_group(id) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        };

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedPermissions() {
        String sql = "INSERT IGNORE INTO permissions (code, name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Permission permission : SYSTEM_PERMISSIONS) {
                pstmt.setString(1, permission.getCode());
                pstmt.setString(2, permission.getName());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureDefaultGroup() {
        String sqlGroup = "INSERT IGNORE INTO user_group (ten_nhom, mo_ta) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlGroup)) {
            pstmt.setString(1, "Trống");
            pstmt.setString(2, "Nhóm mặc định khi user chưa được gán");
            pstmt.executeUpdate();

            pstmt.setString(1, "ADMIN");
            pstmt.setString(2, "Nhóm quản trị viên hệ thống");
            pstmt.executeUpdate();

            pstmt.setString(1, "ACCOUNTANT");
            pstmt.setString(2, "Nhóm kế toán viên");
            pstmt.executeUpdate();

            pstmt.setString(1, "cư dân");
            pstmt.setString(2, "Nhóm cư dân chung cư");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sqlMapping = "INSERT IGNORE INTO user_group_mapping (user_id, group_id) " +
                            "SELECT u.id, g.id FROM users u " +
                            "JOIN user_group g ON g.ten_nhom = u.role COLLATE utf8mb4_unicode_ci " +
                            "WHERE u.username IN ('admin_blue', 'ketoan_moon')";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlMapping);
        } catch (SQLException e) {
            System.err.println("Loi khi tu dong gan nhom mac dinh cho user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
