package com.bluemoon.repositories;

import com.bluemoon.models.ThongBao;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThongBaoRepository {

    public ThongBaoRepository() {
        ensureTable();
    }

    private void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS thong_bao (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "ten_thong_bao VARCHAR(255) NOT NULL, " +
                "file_path VARCHAR(512), " +
                "ngay_ban_hanh DATE, " +
                "trang_thai VARCHAR(50) NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ThongBao> findAll() {
        List<ThongBao> list = new ArrayList<>();
        String sql = "SELECT * FROM thong_bao ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ThongBao thongBao = new ThongBao(
                        rs.getInt("id"),
                        rs.getString("ten_thong_bao"),
                        rs.getString("file_path"),
                        rs.getDate("ngay_ban_hanh") != null ? rs.getDate("ngay_ban_hanh").toLocalDate() : null,
                        rs.getString("trang_thai"));
                list.add(thongBao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(ThongBao thongBao) {
        String sql = "INSERT INTO thong_bao (ten_thong_bao, file_path, ngay_ban_hanh, trang_thai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, thongBao.getTenThongBao());
            pstmt.setString(2, thongBao.getFilePath());
            if (thongBao.getNgayBanHanh() != null) {
                pstmt.setDate(3, Date.valueOf(thongBao.getNgayBanHanh()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            pstmt.setString(4, thongBao.getTrangThai());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ThongBao thongBao) {
        String sql = "UPDATE thong_bao SET ten_thong_bao = ?, file_path = ?, ngay_ban_hanh = ?, trang_thai = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, thongBao.getTenThongBao());
            pstmt.setString(2, thongBao.getFilePath());
            if (thongBao.getNgayBanHanh() != null) {
                pstmt.setDate(3, Date.valueOf(thongBao.getNgayBanHanh()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            pstmt.setString(4, thongBao.getTrangThai());
            pstmt.setInt(5, thongBao.getId());
            return pstmt.executeUpdate() > 0;
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

    public List<ThongBao> search(String tenThongBao, LocalDate ngayBanHanh, String trangThai) {
        List<ThongBao> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM thong_bao WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (tenThongBao != null && !tenThongBao.trim().isEmpty()) {
            sql.append(" AND ten_thong_bao LIKE ?");
            params.add("%" + tenThongBao.trim() + "%");
        }
        if (ngayBanHanh != null) {
            sql.append(" AND ngay_ban_hanh = ?");
            params.add(Date.valueOf(ngayBanHanh));
        }
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            sql.append(" AND trang_thai = ?");
            params.add(trangThai.trim());
        }
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
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
}
