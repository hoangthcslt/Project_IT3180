package com.bluemoon.repositories;

import com.bluemoon.models.KhoanThu;
import com.bluemoon.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
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
                        rs.getString("ghi_chu"),
                        rs.getDate("han_nop") != null ? rs.getDate("han_nop").toLocalDate() : null,
                        rs.getString("trang_thai"));
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
        String sql = "INSERT INTO khoan_thu (ma_khoan_thu, ten_khoan_thu, loai_phi, don_gia, ngay_tao, ghi_chu, han_nop, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, khoanThu.getMaKhoanThu());
            pstmt.setString(2, khoanThu.getTenKhoanThu());
            pstmt.setString(3, khoanThu.getLoaiPhi());
            pstmt.setBigDecimal(4, khoanThu.getDonGia());
            pstmt.setDate(5, Date.valueOf(khoanThu.getNgayTao()));
            pstmt.setString(6, khoanThu.getGhiChu());
            if (khoanThu.getHanNop() != null) {
                pstmt.setDate(7, Date.valueOf(khoanThu.getHanNop()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            pstmt.setString(8, khoanThu.getTrangThai() != null ? khoanThu.getTrangThai() : "DRAFT");

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        khoanThu.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(KhoanThu khoanThu) {
        String sql = "UPDATE khoan_thu SET ma_khoan_thu = ?, ten_khoan_thu = ?, loai_phi = ?, don_gia = ?, ngay_tao = ?, ghi_chu = ?, han_nop = ?, trang_thai = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, khoanThu.getMaKhoanThu());
            pstmt.setString(2, khoanThu.getTenKhoanThu());
            pstmt.setString(3, khoanThu.getLoaiPhi());
            pstmt.setBigDecimal(4, khoanThu.getDonGia());
            if (khoanThu.getNgayTao() != null) {
                pstmt.setDate(5, Date.valueOf(khoanThu.getNgayTao()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            pstmt.setString(6, khoanThu.getGhiChu());
            if (khoanThu.getHanNop() != null) {
                pstmt.setDate(7, Date.valueOf(khoanThu.getHanNop()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            pstmt.setString(8, khoanThu.getTrangThai());
            pstmt.setInt(9, khoanThu.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM khoan_thu WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<KhoanThu> search(String ma, String ten, String loai, String donGiaStr, LocalDate ngayTao) {
        List<KhoanThu> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM khoan_thu WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (ma != null && !ma.trim().isEmpty()) {
            sql.append(" AND ma_khoan_thu LIKE ?");
            params.add("%" + ma.trim() + "%");
        }
        if (ten != null && !ten.trim().isEmpty()) {
            sql.append(" AND ten_khoan_thu LIKE ?");
            params.add("%" + ten.trim() + "%");
        }
        if (loai != null && !loai.trim().isEmpty()) {
            sql.append(" AND loai_phi = ?");
            params.add(loai.trim());
        }
        if (donGiaStr != null && !donGiaStr.trim().isEmpty()) {
            try {
                java.math.BigDecimal donGia = new java.math.BigDecimal(donGiaStr.trim());
                sql.append(" AND don_gia = ?");
                params.add(donGia);
            } catch (NumberFormatException ignored) {
            }
        }
        if (ngayTao != null) {
            sql.append(" AND ngay_tao = ?");
            params.add(Date.valueOf(ngayTao));
        }
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    KhoanThu kt = new KhoanThu(
                            rs.getInt("id"),
                            rs.getString("ma_khoan_thu"),
                            rs.getString("ten_khoan_thu"),
                            rs.getString("loai_phi"),
                            rs.getBigDecimal("don_gia"),
                            rs.getDate("ngay_tao") != null ? rs.getDate("ngay_tao").toLocalDate() : null,
                            rs.getString("ghi_chu"),
                            rs.getDate("han_nop") != null ? rs.getDate("han_nop").toLocalDate() : null,
                            rs.getString("trang_thai"));
                    list.add(kt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tính toán công nợ động dựa trên hóa đơn của hộ khẩu
     */
    public BigDecimal calculateDebt(int hoKhauId, int khoanThuId) {
        String sql = "SELECT tong_tien, so_tien_da_nop FROM hoa_don WHERE ho_khau_id = ? AND khoan_thu_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hoKhauId);
            pstmt.setInt(2, khoanThuId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("tong_tien");
                    BigDecimal paid = rs.getBigDecimal("so_tien_da_nop");
                    BigDecimal debt = total.subtract(paid);
                    return debt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : debt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}
