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
                        rs.getDate("ngay_lap").toLocalDate());
                list.add(hk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isMaHoKhauExists(String maHoKhau) {
        String sql = "SELECT COUNT(*) FROM ho_khau WHERE ma_ho_khau = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoKhau);
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

    public HoKhau findByMaHoKhau(String maHoKhau) {
        String sql = "SELECT * FROM ho_khau WHERE ma_ho_khau = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoKhau);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new HoKhau(
                            rs.getInt("id"),
                            rs.getString("ma_ho_khau"),
                            rs.getString("ten_chu_ho"),
                            rs.getBigDecimal("dien_tich"),
                            rs.getDate("ngay_lap").toLocalDate());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    public boolean update(HoKhau hoKhau) {
        String sql = "UPDATE ho_khau SET ma_ho_khau = ?, ten_chu_ho = ?, dien_tich = ?, ngay_lap = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hoKhau.getMaHoKhau());
            pstmt.setString(2, hoKhau.getTenChuHo());
            pstmt.setBigDecimal(3, hoKhau.getDienTich());
            pstmt.setDate(4, Date.valueOf(hoKhau.getNgayLap()));
            pstmt.setInt(5, hoKhau.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ho_khau WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<HoKhau> search(String maHoKhau, String tenChuHo, String dienTich, java.time.LocalDate ngayLap) {
        List<HoKhau> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ho_khau WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (maHoKhau != null && !maHoKhau.trim().isEmpty()) {
            sql.append(" AND ma_ho_khau LIKE ?");
            params.add("%" + maHoKhau.trim() + "%");
        }
        if (tenChuHo != null && !tenChuHo.trim().isEmpty()) {
            sql.append(" AND ten_chu_ho LIKE ?");
            params.add("%" + tenChuHo.trim() + "%");
        }
        if (dienTich != null && !dienTich.trim().isEmpty()) {
            sql.append(" AND dien_tich = ?");
            params.add(new java.math.BigDecimal(dienTich.trim()));
        }
        if (ngayLap != null) {
            sql.append(" AND ngay_lap = ?");
            params.add(Date.valueOf(ngayLap));
        }
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new HoKhau(
                            rs.getInt("id"),
                            rs.getString("ma_ho_khau"),
                            rs.getString("ten_chu_ho"),
                            rs.getBigDecimal("dien_tich"),
                            rs.getDate("ngay_lap").toLocalDate()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
