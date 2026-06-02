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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentRepository {

    public boolean thucHienThanhToan(int idHoKhau, int idKhoanThu, BigDecimal soTien, String hinhThuc, String nguoiNop) throws SQLException {
        // Find if there is an invoice for this household and fee
        String queryHoaDon = "SELECT id FROM hoa_don WHERE ho_khau_id = ? AND khoan_thu_id = ?";
        int hoaDonId = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryHoaDon)) {
            pstmt.setInt(1, idHoKhau);
            pstmt.setInt(2, idKhoanThu);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    hoaDonId = rs.getInt("id");
                }
            }
        }

        if (hoaDonId > 0) {
            return thucHienThanhToanHoaDon(hoaDonId, soTien, hinhThuc, nguoiNop);
        }

        // Fallback to legacy insertion if no invoice exists (backward compatibility)
        String sql = "INSERT INTO nop_tien (ho_khau_id, khoan_thu_id, nguoi_nop, so_tien_nop, ngay_nop, hinh_thuc) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idHoKhau);
            pstmt.setInt(2, idKhoanThu);
            pstmt.setString(3, nguoiNop);
            pstmt.setBigDecimal(4, soTien);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));
            pstmt.setString(6, hinhThuc);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean thucHienThanhToanHoaDon(int hoaDonId, BigDecimal soTien, String hinhThuc, String nguoiNop) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Fetch invoice and household details to process transaction
            String sqlInv = "SELECT ho_khau_id, khoan_thu_id, tong_tien, so_tien_da_nop FROM hoa_don WHERE id = ?";
            int hoKhauId = 0;
            int khoanThuId = 0;
            BigDecimal tongTien = BigDecimal.ZERO;
            BigDecimal currentPaid = BigDecimal.ZERO;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInv)) {
                pstmt.setInt(1, hoaDonId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        hoKhauId = rs.getInt("ho_khau_id");
                        khoanThuId = rs.getInt("khoan_thu_id");
                        tongTien = rs.getBigDecimal("tong_tien");
                        currentPaid = rs.getBigDecimal("so_tien_da_nop");
                    } else {
                        throw new SQLException("Hóa đơn không tồn tại.");
                    }
                }
            }

            BigDecimal newPaid = currentPaid.add(soTien);
            String trangThai = newPaid.compareTo(tongTien) >= 0 ? "DA_NOP" : "CHUA_NOP";

            // Lệnh 1: UPDATE bảng hóa đơn -> Đổi trạng thái thành "Đã thanh toán" (nếu đã nộp đủ) và cập nhật số tiền đã nộp
            String updateInv = "UPDATE hoa_don SET so_tien_da_nop = ?, trang_thai = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateInv)) {
                pstmt.setBigDecimal(1, newPaid);
                pstmt.setString(2, trangThai);
                pstmt.setInt(3, hoaDonId);
                pstmt.executeUpdate();
            }

            // Lệnh 2: INSERT dữ liệu vào bảng lịch sử giao dịch (thời gian, số tiền, phương thức, mã hóa đơn)
            String insertTx = "INSERT INTO nop_tien (ho_khau_id, khoan_thu_id, nguoi_nop, so_tien_nop, ngay_nop, hinh_thuc, hoa_don_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertTx)) {
                pstmt.setInt(1, hoKhauId);
                pstmt.setInt(2, khoanThuId);
                pstmt.setString(3, nguoiNop);
                pstmt.setBigDecimal(4, soTien);
                pstmt.setDate(5, Date.valueOf(LocalDate.now())); // Thời gian
                pstmt.setString(6, hinhThuc);                   // Phương thức
                pstmt.setInt(7, hoaDonId);                      // Liên kết hóa đơn (mã hóa đơn tương ứng)
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<Map<String, Object>> findGiaoDichHistory(String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT nt.*, hk.ma_ho_khau, hk.ten_chu_ho, kt.ten_khoan_thu, hd.ma_hoa_don FROM nop_tien nt " +
                "JOIN ho_khau hk ON nt.ho_khau_id = hk.id " +
                "JOIN khoan_thu kt ON nt.khoan_thu_id = kt.id " +
                "LEFT JOIN hoa_don hd ON (nt.hoa_don_id = hd.id OR (nt.hoa_don_id IS NULL AND nt.ho_khau_id = hd.ho_khau_id AND nt.khoan_thu_id = hd.khoan_thu_id)) " +
                "WHERE nt.so_tien_nop > 0"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (hk.ma_ho_khau LIKE ? OR hk.ten_chu_ho LIKE ? OR kt.ten_khoan_thu LIKE ? OR nt.nguoi_nop LIKE ?)");
            String val = "%" + keyword.trim() + "%";
            params.add(val);
            params.add(val);
            params.add(val);
            params.add(val);
        }
        sql.append(" ORDER BY nt.id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("maHoKhau", rs.getString("ma_ho_khau"));
                    map.put("tenChuHo", rs.getString("ten_chu_ho"));
                    map.put("tenKhoanThu", rs.getString("ten_khoan_thu"));
                    map.put("maHoaDon", rs.getString("ma_hoa_don") != null ? rs.getString("ma_hoa_don") : "N/A");
                    map.put("nguoiNop", rs.getString("nguoi_nop"));
                    map.put("soTienNop", rs.getBigDecimal("so_tien_nop"));
                    map.put("ngayNop", rs.getDate("ngay_nop").toLocalDate());
                    map.put("hinhThuc", rs.getString("hinh_thuc"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countPaidItems() {
        String sql = "SELECT COUNT(*) FROM hoa_don WHERE trang_thai = 'DA_NOP'";
        return countByQuery(sql);
    }

    public int countUnpaidItems() {
        String sql = "SELECT COUNT(*) FROM hoa_don WHERE trang_thai = 'CHUA_NOP'";
        return countByQuery(sql);
    }

    public List<PaymentStatusView> findPaidItems(String keyword) {
        String sql = "SELECT hd.id, hk.ma_ho_khau, hk.ten_chu_ho, kt.ten_khoan_thu, hd.tong_tien AS so_tien, " +
                     " (SELECT MAX(ngay_nop) FROM nop_tien WHERE hoa_don_id = hd.id) AS ngay_nop, " +
                     " hd.han_nop AS han_dong, 'Da nop' AS trang_thai " +
                     "FROM hoa_don hd " +
                     "JOIN ho_khau hk ON hd.ho_khau_id = hk.id " +
                     "JOIN khoan_thu kt ON hd.khoan_thu_id = kt.id " +
                     "WHERE hd.trang_thai = 'DA_NOP' " +
                     "AND (? IS NULL OR hk.ma_ho_khau LIKE ? OR hk.ten_chu_ho LIKE ? OR kt.ten_khoan_thu LIKE ?)";
        return findPaymentStatusByQuery(sql, keyword);
    }

    public List<PaymentStatusView> findUnpaidItems(String keyword) {
        String sql = "SELECT hd.id, hk.ma_ho_khau, hk.ten_chu_ho, kt.ten_khoan_thu, (hd.tong_tien - hd.so_tien_da_nop) AS so_tien, " +
                     " NULL AS ngay_nop, hd.han_nop AS han_dong, 'Chua nop' AS trang_thai " +
                     "FROM hoa_don hd " +
                     "JOIN ho_khau hk ON hd.ho_khau_id = hk.id " +
                     "JOIN khoan_thu kt ON hd.khoan_thu_id = kt.id " +
                     "WHERE hd.trang_thai = 'CHUA_NOP' " +
                     "AND (? IS NULL OR hk.ma_ho_khau LIKE ? OR hk.ten_chu_ho LIKE ? OR kt.ten_khoan_thu LIKE ?)";
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
