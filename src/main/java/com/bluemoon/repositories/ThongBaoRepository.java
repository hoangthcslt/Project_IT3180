package com.bluemoon.repositories;

import com.bluemoon.models.ThongBao;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThongBaoRepository {

    public List<ThongBao> findAll() {
        List<ThongBao> list = new ArrayList<>();
        String sql = notificationSelect() + " GROUP BY tb.id ORDER BY tb.id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(ThongBao thongBao) {
        return insert(thongBao, new ArrayList<>());
    }

    public boolean insert(ThongBao thongBao, List<Integer> groupIds) {
        String sql = "INSERT INTO thong_bao (ten_thong_bao, file_path, ngay_ban_hanh, trang_thai) VALUES (?, ?, ?, ?)";
        String sqlGroup = "INSERT INTO thong_bao_group (thong_bao_id, group_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement pstmtGroup = conn.prepareStatement(sqlGroup)) {

                pstmt.setString(1, thongBao.getTenThongBao());
                pstmt.setString(2, thongBao.getFilePath());
                if (thongBao.getNgayBanHanh() != null) {
                    pstmt.setDate(3, Date.valueOf(thongBao.getNgayBanHanh()));
                } else {
                    pstmt.setNull(3, Types.DATE);
                }
                pstmt.setString(4, thongBao.getTrangThai());
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                int thongBaoId = -1;
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        thongBaoId = rs.getInt(1);
                    }
                }

                if (thongBaoId != -1 && groupIds != null && !groupIds.isEmpty()) {
                    for (int gId : groupIds) {
                        pstmtGroup.setInt(1, thongBaoId);
                        pstmtGroup.setInt(2, gId);
                        pstmtGroup.addBatch();
                    }
                    pstmtGroup.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ThongBao thongBao) {
        return update(thongBao, getGroupIdsByNotification(thongBao.getId()));
    }

    public boolean update(ThongBao thongBao, List<Integer> groupIds) {
        String sql = "UPDATE thong_bao SET ten_thong_bao = ?, file_path = ?, ngay_ban_hanh = ?, trang_thai = ? WHERE id = ?";
        String sqlDelete = "DELETE FROM thong_bao_group WHERE thong_bao_id = ?";
        String sqlInsert = "INSERT INTO thong_bao_group (thong_bao_id, group_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                    PreparedStatement pstmtDel = conn.prepareStatement(sqlDelete);
                    PreparedStatement pstmtIns = conn.prepareStatement(sqlInsert)) {

                pstmt.setString(1, thongBao.getTenThongBao());
                pstmt.setString(2, thongBao.getFilePath());
                if (thongBao.getNgayBanHanh() != null) {
                    pstmt.setDate(3, Date.valueOf(thongBao.getNgayBanHanh()));
                } else {
                    pstmt.setNull(3, Types.DATE);
                }
                pstmt.setString(4, thongBao.getTrangThai());
                pstmt.setInt(5, thongBao.getId());
                pstmt.executeUpdate();

                pstmtDel.setInt(1, thongBao.getId());
                pstmtDel.executeUpdate();

                if (groupIds != null && !groupIds.isEmpty()) {
                    for (int gId : groupIds) {
                        pstmtIns.setInt(1, thongBao.getId());
                        pstmtIns.setInt(2, gId);
                        pstmtIns.addBatch();
                    }
                    pstmtIns.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM thong_bao WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Integer> getGroupIdsByNotification(int thongBaoId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT group_id FROM thong_bao_group WHERE thong_bao_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, thongBaoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("group_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ThongBao> findNotificationsForUser(int userId) {
        List<ThongBao> list = new ArrayList<>();
        String sql = "SELECT DISTINCT tb.* " +
                "FROM thong_bao tb " +
                "JOIN thong_bao_group tbg ON tb.id = tbg.thong_bao_id " +
                "JOIN user_group_mapping ugm ON tbg.group_id = ugm.group_id " +
                "WHERE ugm.user_id = ? AND tb.trang_thai IN ('Đã xuất bản', 'Đã phát hành') " +
                "ORDER BY tb.ngay_ban_hanh DESC, tb.id DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ThongBao thongBao = new ThongBao(
                            rs.getInt("id"),
                            rs.getString("ten_thong_bao"),
                            rs.getString("file_path"),
                            rs.getDate("ngay_ban_hanh") != null ? rs.getDate("ngay_ban_hanh").toLocalDate() : null,
                            rs.getString("trang_thai"));
                    list.add(thongBao);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ThongBao> search(String tenThongBao, LocalDate ngayBanHanh, String trangThai, Integer groupId) {
        List<ThongBao> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(notificationSelect()).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (tenThongBao != null && !tenThongBao.trim().isEmpty()) {
            sql.append(" AND tb.ten_thong_bao LIKE ?");
            params.add("%" + tenThongBao.trim() + "%");
        }
        if (ngayBanHanh != null) {
            sql.append(" AND tb.ngay_ban_hanh = ?");
            params.add(Date.valueOf(ngayBanHanh));
        }
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            sql.append(" AND tb.trang_thai = ?");
            params.add(trangThai.trim());
        }
        if (groupId != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM thong_bao_group filter_group WHERE filter_group.thong_bao_id = tb.id AND filter_group.group_id = ?)");
            params.add(groupId);
        }
        sql.append(" GROUP BY tb.id ORDER BY tb.id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markAsRead(int userId, int thongBaoId) {
        String sql = "INSERT IGNORE INTO user_read_notification (user_id, thong_bao_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, thongBaoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Integer> getReadNotificationIds(int userId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT thong_bao_id FROM user_read_notification WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("thong_bao_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String notificationSelect() {
        return "SELECT tb.*, COALESCE(GROUP_CONCAT(DISTINCT ug.ten_nhom ORDER BY ug.ten_nhom SEPARATOR ', '), 'Chưa chọn') AS nhom_nhan "
                + "FROM thong_bao tb LEFT JOIN thong_bao_group tbg ON tb.id = tbg.thong_bao_id "
                + "LEFT JOIN user_group ug ON ug.id = tbg.group_id";
    }

    private ThongBao map(ResultSet rs) throws SQLException {
        ThongBao item = new ThongBao(rs.getInt("id"), rs.getString("ten_thong_bao"), rs.getString("file_path"),
                rs.getDate("ngay_ban_hanh") == null ? null : rs.getDate("ngay_ban_hanh").toLocalDate(),
                rs.getString("trang_thai"));
        item.setNhomNhan(rs.getString("nhom_nhan"));
        return item;
    }
}
