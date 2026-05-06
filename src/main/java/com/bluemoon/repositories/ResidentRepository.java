package com.bluemoon.repositories;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
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
                            rs.getDate("ngay_sinh").toLocalDate(),
                            rs.getString("gioi_tinh"),
                            rs.getString("quan_he"),
                            rs.getString("trang_thai")
                    );
                    list.add(nk);
                }
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
}
