package com.bluemoon.repositories;

import com.bluemoon.models.HoKhau;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HouseholdRepository {

    public List<HoKhau> findAll() {
        List<HoKhau> list = new ArrayList<>();
        String sql = "SELECT * FROM ho_khau ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                HoKhau hk = new HoKhau(
                        rs.getInt("id"),
                        rs.getString("ma_ho_khau"),
                        rs.getString("ten_chu_ho"),
                        rs.getBigDecimal("dien_tich"),
                        rs.getDate("ngay_lap").toLocalDate()
                );
                list.add(hk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(HoKhau hoKhau) {
        String sql = "INSERT INTO ho_khau (ma_ho_khau, ten_chu_ho, dien_tich, ngay_lap) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hoKhau.getMaHoKhau());
            pstmt.setString(2, hoKhau.getTenChuHo());
            pstmt.setBigDecimal(3, hoKhau.getDienTich());
            pstmt.setDate(4, Date.valueOf(hoKhau.getNgayLap()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
