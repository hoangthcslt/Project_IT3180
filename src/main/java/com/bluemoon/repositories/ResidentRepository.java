package com.bluemoon.repositories;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResidentRepository {

    public List<NhanKhau> findByHoKhauId(int hoKhauId) {
        List<NhanKhau> list = new ArrayList<>();
        String sql = "SELECT * FROM nhan_khau WHERE ho_khau_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hoKhauId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NhanKhau nk = new NhanKhau(
                            rs.getInt("id"),
                            rs.getInt("ho_khau_id"),
                            rs.getString("ho_ten"),
                            rs.getString("cccd"),
                            rs.getDate("ngay_sinh") != null ? rs.getDate("ngay_sinh").toLocalDate() : null,
                            rs.getString("gioi_tinh"),
                            rs.getString("quan_he"),
                            rs.getString("trang_thai"));
                    list.add(nk);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<NhanKhau> findAll() {
        List<NhanKhau> list = new ArrayList<>();
        String sql = "SELECT * FROM nhan_khau ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                NhanKhau nk = new NhanKhau(
                        rs.getInt("id"),
                        rs.getInt("ho_khau_id"),
                        rs.getString("ho_ten"),
                        rs.getString("cccd"),
                        rs.getDate("ngay_sinh") != null ? rs.getDate("ngay_sinh").toLocalDate() : null,
                        rs.getString("gioi_tinh"),
                        rs.getString("quan_he"),
                        rs.getString("trang_thai"));
                list.add(nk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(NhanKhau nhanKhau) {
        String sql = "INSERT INTO nhan_khau (ho_khau_id, ho_ten, cccd, ngay_sinh, gioi_tinh, quan_he, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nhanKhau.getHoKhauId());
            pstmt.setString(2, nhanKhau.getHoTen());
            pstmt.setString(3, nhanKhau.getCccd());
            pstmt.setDate(4, Date.valueOf(nhanKhau.getNgaySinh()));
            pstmt.setString(5, nhanKhau.getGioiTinh());
            pstmt.setString(6, nhanKhau.getQuanHe());
            pstmt.setString(7, nhanKhau.getTrangThai());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(NhanKhau nhanKhau) {
        String sql = "UPDATE nhan_khau SET ho_khau_id = ?, ho_ten = ?, cccd = ?, ngay_sinh = ?, gioi_tinh = ?, quan_he = ?, trang_thai = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nhanKhau.getHoKhauId());
            pstmt.setString(2, nhanKhau.getHoTen());
            pstmt.setString(3, nhanKhau.getCccd());
            if (nhanKhau.getNgaySinh() != null) {
                pstmt.setDate(4, Date.valueOf(nhanKhau.getNgaySinh()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setString(5, nhanKhau.getGioiTinh());
            pstmt.setString(6, nhanKhau.getQuanHe());
            pstmt.setString(7, nhanKhau.getTrangThai());
            pstmt.setInt(8, nhanKhau.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM nhan_khau WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<NhanKhau> search(String hoKhauId, String hoTen, String cccd, LocalDate ngaySinh) {
        List<NhanKhau> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM nhan_khau WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (hoKhauId != null && !hoKhauId.trim().isEmpty()) {
            sql.append(" AND ho_khau_id = ?");
            params.add(Integer.parseInt(hoKhauId.trim()));
        }
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            sql.append(" AND ho_ten LIKE ?");
            params.add("%" + hoTen.trim() + "%");
        }
        if (cccd != null && !cccd.trim().isEmpty()) {
            sql.append(" AND cccd LIKE ?");
            params.add("%" + cccd.trim() + "%");
        }
        if (ngaySinh != null) {
            sql.append(" AND ngay_sinh = ?");
            params.add(Date.valueOf(ngaySinh));
        }
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NhanKhau nk = new NhanKhau(
                            rs.getInt("id"),
                            rs.getInt("ho_khau_id"),
                            rs.getString("ho_ten"),
                            rs.getString("cccd"),
                            rs.getDate("ngay_sinh") != null ? rs.getDate("ngay_sinh").toLocalDate() : null,
                            rs.getString("gioi_tinh"),
                            rs.getString("quan_he"),
                            rs.getString("trang_thai"));
                    list.add(nk);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
