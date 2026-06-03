package com.bluemoon.repositories;

import com.bluemoon.models.DanhMucPhi;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucPhiRepository {

    public List<DanhMucPhi> findAll() {
        List<DanhMucPhi> list = new ArrayList<>();
        String sql = "SELECT * FROM danh_muc_phi ORDER BY ma_phi ASC";
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

    public boolean isMaPhiExists(String maPhi) {
        String sql = "SELECT COUNT(*) FROM danh_muc_phi WHERE ma_phi = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maPhi);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(DanhMucPhi phi) {
        String sql = "INSERT INTO danh_muc_phi (ma_phi, ten_phi, loai_phi, loai_tinh_gia, don_gia, ghi_chu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phi.getMaPhi());
            pstmt.setString(2, phi.getTenPhi());
            pstmt.setString(3, phi.getLoaiPhi());
            pstmt.setString(4, phi.getLoaiTinhGia());
            pstmt.setBigDecimal(5, phi.getDonGia());
            pstmt.setString(6, phi.getGhiChu());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(DanhMucPhi phi) {
        String sql = "UPDATE danh_muc_phi SET ma_phi = ?, ten_phi = ?, loai_phi = ?, loai_tinh_gia = ?, don_gia = ?, ghi_chu = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phi.getMaPhi());
            pstmt.setString(2, phi.getTenPhi());
            pstmt.setString(3, phi.getLoaiPhi());
            pstmt.setString(4, phi.getLoaiTinhGia());
            pstmt.setBigDecimal(5, phi.getDonGia());
            pstmt.setString(6, phi.getGhiChu());
            pstmt.setInt(7, phi.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM danh_muc_phi WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<DanhMucPhi> search(String ma, String ten, String loai, String loaiTinh) {
        List<DanhMucPhi> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM danh_muc_phi WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (ma != null && !ma.trim().isEmpty()) {
            sql.append(" AND ma_phi LIKE ?");
            params.add("%" + ma.trim() + "%");
        }
        if (ten != null && !ten.trim().isEmpty()) {
            sql.append(" AND ten_phi LIKE ?");
            params.add("%" + ten.trim() + "%");
        }
        if (loai != null && !loai.trim().isEmpty()) {
            sql.append(" AND loai_phi = ?");
            params.add(loai.trim());
        }
        if (loaiTinh != null && !loaiTinh.trim().isEmpty()) {
            sql.append(" AND loai_tinh_gia = ?");
            params.add(loaiTinh.trim());
        }
        sql.append(" ORDER BY ma_phi ASC");

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

    private DanhMucPhi map(ResultSet rs) throws SQLException {
        return new DanhMucPhi(
                rs.getInt("id"),
                rs.getString("ma_phi"),
                rs.getString("ten_phi"),
                rs.getString("loai_phi"),
                rs.getString("loai_tinh_gia"),
                rs.getBigDecimal("don_gia"),
                rs.getString("ghi_chu")
        );
    }
}
