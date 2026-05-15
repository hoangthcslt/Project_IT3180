package com.bluemoon.repositories;

import com.bluemoon.models.KhoanThu;
import com.bluemoon.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeeRepository {

    public List<KhoanThu> findAll() {
        List<KhoanThu> list = new ArrayList<>();
        String sql = "SELECT * FROM khoan_thu ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                KhoanThu kt = new KhoanThu(
                        rs.getInt("id"),
                        rs.getString("ma_khoan_thu"),
                        rs.getString("ten_khoan_thu"),
                        rs.getString("loai_phi"),
                        rs.getBigDecimal("don_gia"),
                        rs.getDate("ngay_tao").toLocalDate(),
                        rs.getString("ghi_chu")
                );
                list.add(kt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isMaKhoanThuExists(String maKhoanThu) {
        String sql = "SELECT COUNT(*) FROM khoan_thu WHERE ma_khoan_thu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKhoanThu);
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

    public boolean insert(KhoanThu khoanThu) {
        String sql = "INSERT INTO khoan_thu (ma_khoan_thu, ten_khoan_thu, loai_phi, don_gia, ngay_tao, ghi_chu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, khoanThu.getMaKhoanThu());
            pstmt.setString(2, khoanThu.getTenKhoanThu());
            pstmt.setString(3, khoanThu.getLoaiPhi());
            pstmt.setBigDecimal(4, khoanThu.getDonGia());
            pstmt.setDate(5, Date.valueOf(khoanThu.getNgayTao()));
            pstmt.setString(6, khoanThu.getGhiChu());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tính toán công nợ động (Diện tích * Đơn giá - Đã nộp)
     */
    public BigDecimal calculateDebt(int hoKhauId, int khoanThuId) {
        String sql = "SELECT " +
                     "   hk.dien_tich, " +
                     "   kt.don_gia, " +
                     "   COALESCE(SUM(nt.so_tien_nop), 0) AS tong_da_nop " +
                     "FROM ho_khau hk " +
                     "CROSS JOIN khoan_thu kt " +
                     "LEFT JOIN nop_tien nt ON nt.ho_khau_id = hk.id AND nt.khoan_thu_id = kt.id " +
                     "WHERE hk.id = ? AND kt.id = ?";
                     
        BigDecimal debt = BigDecimal.ZERO;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hoKhauId);
            pstmt.setInt(2, khoanThuId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal dienTich = rs.getBigDecimal("dien_tich");
                    BigDecimal donGia = rs.getBigDecimal("don_gia");
                    BigDecimal tongDaNop = rs.getBigDecimal("tong_da_nop");
                    
                    if (dienTich != null && donGia != null) {
                        BigDecimal totalFee = dienTich.multiply(donGia);
                        debt = totalFee.subtract(tongDaNop != null ? tongDaNop : BigDecimal.ZERO);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Nếu số tiền nợ < 0 (nộp dư), có thể trả về 0 hoặc giữ nguyên số âm tùy logic
        return debt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : debt;
    }
}
