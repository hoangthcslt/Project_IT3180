package com.bluemoon.repositories;

import com.bluemoon.models.PaymentStatusView;
import com.bluemoon.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {

    public boolean thucHienThanhToan(int idHoKhau, int idKhoanThu, BigDecimal soTien, String hinhThuc, String nguoiNop) throws SQLException {
        String sql = "INSERT INTO nop_tien (ho_khau_id, khoan_thu_id, nguoi_nop, so_tien_nop, ngay_nop, hinh_thuc) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, idHoKhau);
            pstmt.setInt(2, idKhoanThu);
            pstmt.setString(3, nguoiNop);
            pstmt.setBigDecimal(4, soTien);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));
            pstmt.setString(6, hinhThuc);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public int countPaidItems() {
        String sql = "SELECT COUNT(*) FROM ( " +
                     "SELECT hk.id AS ho_khau_id, kt.id AS khoan_thu_id " +
                     "FROM ho_khau hk " +
                     "CROSS JOIN khoan_thu kt " +
                     "LEFT JOIN nop_tien nt ON nt.ho_khau_id = hk.id AND nt.khoan_thu_id = kt.id " +
                     "GROUP BY hk.id, kt.id, hk.dien_tich, kt.don_gia " +
                     "HAVING COALESCE(SUM(nt.so_tien_nop), 0) >= hk.dien_tich * kt.don_gia " +
                     ") paid_items";

        return countByQuery(sql);
    }

    public int countUnpaidItems() {
        String sql = "SELECT COUNT(*) FROM ( " +
                     "SELECT hk.id AS ho_khau_id, kt.id AS khoan_thu_id " +
                     "FROM ho_khau hk " +
                     "CROSS JOIN khoan_thu kt " +
                     "LEFT JOIN nop_tien nt ON nt.ho_khau_id = hk.id AND nt.khoan_thu_id = kt.id " +
                     "GROUP BY hk.id, kt.id, hk.dien_tich, kt.don_gia " +
                     "HAVING COALESCE(SUM(nt.so_tien_nop), 0) < hk.dien_tich * kt.don_gia " +
                     ") unpaid_items";

        return countByQuery(sql);
    }

    public List<PaymentStatusView> findPaidItems(String keyword) {
        String sql = "SELECT hk.id, hk.ma_ho_khau, hk.ten_chu_ho, kt.ten_khoan_thu, " +
                     "hk.dien_tich * kt.don_gia AS so_tien, MAX(nt.ngay_nop) AS ngay_nop, kt.ngay_tao AS han_dong, " +
                     "'Da nop' AS trang_thai " +
                     "FROM ho_khau hk " +
                     "CROSS JOIN khoan_thu kt " +
                     "LEFT JOIN nop_tien nt ON nt.ho_khau_id = hk.id AND nt.khoan_thu_id = kt.id " +
                     "WHERE (? IS NULL OR hk.ma_ho_khau LIKE ? OR hk.ten_chu_ho LIKE ? OR kt.ten_khoan_thu LIKE ? OR 'Da nop' LIKE ?) " +
                     "GROUP BY hk.id, hk.ma_ho_khau, hk.ten_chu_ho, kt.id, kt.ten_khoan_thu, hk.dien_tich, kt.don_gia, kt.ngay_tao " +
                     "HAVING COALESCE(SUM(nt.so_tien_nop), 0) >= hk.dien_tich * kt.don_gia " +
                     "ORDER BY ngay_nop DESC, hk.id DESC";
        return findPaymentStatusByQuery(sql, keyword);
    }

    public List<PaymentStatusView> findUnpaidItems(String keyword) {
        String sql = "SELECT hk.id, hk.ma_ho_khau, hk.ten_chu_ho, kt.ten_khoan_thu, " +
                     "GREATEST(hk.dien_tich * kt.don_gia - COALESCE(SUM(nt.so_tien_nop), 0), 0) AS so_tien, " +
                     "NULL AS ngay_nop, kt.ngay_tao AS han_dong, 'Chua nop' AS trang_thai " +
                     "FROM ho_khau hk " +
                     "CROSS JOIN khoan_thu kt " +
                     "LEFT JOIN nop_tien nt ON nt.ho_khau_id = hk.id AND nt.khoan_thu_id = kt.id " +
                     "WHERE (? IS NULL OR hk.ma_ho_khau LIKE ? OR hk.ten_chu_ho LIKE ? OR kt.ten_khoan_thu LIKE ?) " +
                     "GROUP BY hk.id, hk.ma_ho_khau, hk.ten_chu_ho, kt.id, kt.ten_khoan_thu, hk.dien_tich, kt.don_gia, kt.ngay_tao " +
                     "HAVING COALESCE(SUM(nt.so_tien_nop), 0) < hk.dien_tich * kt.don_gia " +
                     "ORDER BY hk.id DESC, kt.id DESC";
        return findPaymentStatusByQuery(sql, keyword);
    }

    private int countByQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<PaymentStatusView> findPaymentStatusByQuery(String sql, String keyword) {
        List<PaymentStatusView> items = new ArrayList<>();
        String value = keyword == null || keyword.trim().isEmpty() ? null : "%" + keyword.trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int params = pstmt.getParameterMetaData().getParameterCount();
            for (int i = 1; i <= params; i++) {
                pstmt.setString(i, value);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Date ngayNop = rs.getDate("ngay_nop");
                    Date hanDong = rs.getDate("han_dong");
                    items.add(new PaymentStatusView(
                            rs.getInt("id"),
                            rs.getString("ma_ho_khau"),
                            rs.getString("ten_chu_ho"),
                            rs.getString("ten_khoan_thu"),
                            rs.getBigDecimal("so_tien"),
                            ngayNop == null ? null : ngayNop.toLocalDate(),
                            hanDong == null ? null : hanDong.toLocalDate(),
                            rs.getString("trang_thai")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
