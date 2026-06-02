package com.bluemoon.repositories;

import com.bluemoon.models.HoaDon;
import com.bluemoon.models.ChiTietHoaDon;
import com.bluemoon.models.DanhMucPhi;
import com.bluemoon.models.HoKhau;
import com.bluemoon.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonRepository {

    public List<HoaDon> findAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, hk.ma_ho_khau, hk.ten_chu_ho FROM hoa_don hd " +
                     "JOIN ho_khau hk ON hd.ho_khau_id = hk.id ORDER BY hd.id DESC";
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

    public List<HoaDon> findByKhoanThuId(int khoanThuId) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, hk.ma_ho_khau, hk.ten_chu_ho FROM hoa_don hd " +
                     "JOIN ho_khau hk ON hd.ho_khau_id = hk.id " +
                     "WHERE hd.khoan_thu_id = ? ORDER BY hk.ma_ho_khau ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, khoanThuId);
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

    public List<ChiTietHoaDon> findDetailsByHoaDonId(int hoaDonId) {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM chi_tiet_hoa_don WHERE hoa_don_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hoaDonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChiTietHoaDon(
                            rs.getInt("id"),
                            rs.getInt("hoa_don_id"),
                            rs.getString("ma_phi"),
                            rs.getString("ten_phi"),
                            rs.getBigDecimal("don_gia"),
                            rs.getBigDecimal("so_luong"),
                            rs.getBigDecimal("thanh_tien")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createDraftInvoicesForRun(int khoanThuId, LocalDate ngayTao, LocalDate hanNop) throws SQLException {
        // Fetch all active households
        List<HoKhau> households = new ArrayList<>();
        String queryHouseholds = "SELECT * FROM ho_khau";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryHouseholds);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                households.add(new HoKhau(
                        rs.getInt("id"),
                        rs.getString("ma_ho_khau"),
                        rs.getString("ten_chu_ho"),
                        rs.getBigDecimal("dien_tich"),
                        rs.getString("status"),
                        rs.getInt("so_nguoi"),
                        rs.getString("phuong_tien"),
                        rs.getDate("ngay_lap").toLocalDate(),
                        rs.getInt("so_xe_may"),
                        rs.getInt("so_oto")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        // Fetch all fee templates
        List<DanhMucPhi> feeTemplates = new ArrayList<>();
        String queryFees = "SELECT * FROM danh_muc_phi";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryFees);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                feeTemplates.add(new DanhMucPhi(
                        rs.getInt("id"),
                        rs.getString("ma_phi"),
                        rs.getString("ten_phi"),
                        rs.getString("loai_phi"),
                        rs.getString("loai_tinh_gia"),
                        rs.getBigDecimal("don_gia"),
                        rs.getString("ghi_chu")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String insertInvoice = "INSERT INTO hoa_don (khoan_thu_id, ho_khau_id, ma_hoa_don, tong_tien, so_tien_da_nop, trang_thai, ngay_tao, han_nop) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String insertDetail = "INSERT INTO chi_tiet_hoa_don (hoa_don_id, ma_phi, ten_phi, don_gia, so_luong, thanh_tien) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psInv = conn.prepareStatement(insertInvoice, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psDet = conn.prepareStatement(insertDetail)) {

                for (HoKhau hk : households) {
                    String maHoaDon = "HD-" + khoanThuId + "-" + hk.getMaHoKhau();
                    psInv.setInt(1, khoanThuId);
                    psInv.setInt(2, hk.getId());
                    psInv.setString(3, maHoaDon);
                    psInv.setBigDecimal(4, BigDecimal.ZERO);
                    psInv.setBigDecimal(5, BigDecimal.ZERO);
                    psInv.setString(6, "CHUA_NOP");
                    psInv.setDate(7, Date.valueOf(ngayTao));
                    psInv.setDate(8, Date.valueOf(hanNop));
                    psInv.executeUpdate();

                    int hoaDonId = 0;
                    try (ResultSet generatedKeys = psInv.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            hoaDonId = generatedKeys.getInt(1);
                        }
                    }

                    BigDecimal totalInvoiceAmount = BigDecimal.ZERO;

                    for (DanhMucPhi dm : feeTemplates) {
                        BigDecimal qty = BigDecimal.ZERO;
                        BigDecimal rate = dm.getDonGia();

                        if ("THEO_DIEN_TICH".equals(dm.getLoaiTinhGia())) {
                            qty = hk.getDienTich();
                            if (qty == null) qty = BigDecimal.ZERO;
                        } else if ("THEO_SO_NGUOI".equals(dm.getLoaiTinhGia())) {
                            qty = BigDecimal.valueOf(hk.getSoNguoi());
                        } else if ("CO_DINH".equals(dm.getLoaiTinhGia())) {
                            qty = BigDecimal.ONE;
                        } else if ("NHAP_TAY".equals(dm.getLoaiTinhGia())) {
                            // Manual fields are initialized to 0
                            qty = BigDecimal.ZERO;
                        }

                        // Special case: Parking Fee (GUI_XE or custom logic)
                        // If we have distinct rates for motorbike and car:
                        // In v2, parking fee is calculated as: number of motorbikes * don_gia_xe_may + number of cars * don_gia_oto.
                        // Let's check how we handle it:
                        // If ma_phi is 'GUI_XE' (or similar), we could add specific rows for 'XE_MAY' and 'OTO'.
                        // To make it fully dynamic and compliant with the user request:
                        // Let's create two fee definitions in `danh_muc_phi`:
                        // 1. 'XEMAY': Phí gửi xe máy, loai_tinh_gia = 'CO_DINH' (flat, but we set quantity = hk.getSoXeMay())
                        // 2. 'OTO': Phí gửi xe ô tô, loai_tinh_gia = 'CO_DINH' (flat, but we set quantity = hk.getSoOto())
                        // Let's check if the table has these or if we should map them:
                        if ("XEMAY".equals(dm.getMaPhi())) {
                            qty = BigDecimal.valueOf(hk.getSoXeMay());
                        } else if ("OTO".equals(dm.getMaPhi())) {
                            qty = BigDecimal.valueOf(hk.getSoOto());
                        }

                        BigDecimal itemTotal = rate.multiply(qty);
                        totalInvoiceAmount = totalInvoiceAmount.add(itemTotal);

                        psDet.setInt(1, hoaDonId);
                        psDet.setString(2, dm.getMaPhi());
                        psDet.setString(3, dm.getTenPhi());
                        psDet.setBigDecimal(4, rate);
                        psDet.setBigDecimal(5, qty);
                        psDet.setBigDecimal(6, itemTotal);
                        psDet.executeUpdate();
                    }

                    // Update invoice total
                    String updateInvoiceTotal = "UPDATE hoa_don SET tong_tien = ? WHERE id = ?";
                    try (PreparedStatement psUp = conn.prepareStatement(updateInvoiceTotal)) {
                        psUp.setBigDecimal(1, totalInvoiceAmount);
                        psUp.setInt(2, hoaDonId);
                        psUp.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean saveInvoiceDetails(int hoaDonId, List<ChiTietHoaDon> details) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String updateDetail = "UPDATE chi_tiet_hoa_don SET so_luong = ?, thanh_tien = ? WHERE id = ?";
            BigDecimal totalInvoiceAmount = BigDecimal.ZERO;

            try (PreparedStatement pstmt = conn.prepareStatement(updateDetail)) {
                for (ChiTietHoaDon det : details) {
                    BigDecimal rate = det.getDonGia();
                    BigDecimal qty = det.getSoLuong();
                    BigDecimal itemTotal = rate.multiply(qty);

                    pstmt.setBigDecimal(1, qty);
                    pstmt.setBigDecimal(2, itemTotal);
                    pstmt.setInt(3, det.getId());
                    pstmt.executeUpdate();

                    totalInvoiceAmount = totalInvoiceAmount.add(itemTotal);
                }
            }

            // Update invoice total
            String updateInvoiceTotal = "UPDATE hoa_don SET tong_tien = ? WHERE id = ?";
            try (PreparedStatement psUp = conn.prepareStatement(updateInvoiceTotal)) {
                psUp.setBigDecimal(1, totalInvoiceAmount);
                psUp.setInt(2, hoaDonId);
                psUp.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public List<HoaDon> searchInvoices(String keyword, String trangThai, String loaiPhi) {
        List<HoaDon> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT hd.*, hk.ma_ho_khau, hk.ten_chu_ho FROM hoa_don hd " +
                "JOIN ho_khau hk ON hd.ho_khau_id = hk.id " +
                "JOIN khoan_thu kt ON hd.khoan_thu_id = kt.id " +
                "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (hk.ma_ho_khau LIKE ? OR hk.ten_chu_ho LIKE ? OR hd.ma_hoa_don LIKE ?)");
            String val = "%" + keyword.trim() + "%";
            params.add(val);
            params.add(val);
            params.add(val);
        }
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            sql.append(" AND hd.trang_thai = ?");
            params.add(trangThai.trim());
        }
        if (loaiPhi != null && !loaiPhi.trim().isEmpty()) {
            sql.append(" AND kt.loai_phi = ?");
            params.add(loaiPhi.trim());
        }
        // Only show published invoices unless we are viewing drafts
        // Actually, we can show all or only published based on requirements.
        // For PaymentController, we only show published invoices.
        // Let's add that filter
        sql.append(" AND kt.trang_thai = 'PUBLISHED'");
        sql.append(" ORDER BY hd.id DESC");

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

    public boolean updatePayment(int hoaDonId, BigDecimal amountPaid) {
        String query = "SELECT tong_tien, so_tien_da_nop FROM hoa_don WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, hoaDonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("tong_tien");
                    BigDecimal currentPaid = rs.getBigDecimal("so_tien_da_nop");
                    BigDecimal newPaid = currentPaid.add(amountPaid);

                    String updateSql = "UPDATE hoa_don SET so_tien_da_nop = ?, trang_thai = ? WHERE id = ?";
                    try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                        psUp.setBigDecimal(1, newPaid);
                        psUp.setString(2, newPaid.compareTo(total) >= 0 ? "DA_NOP" : "CHUA_NOP");
                        psUp.setInt(3, hoaDonId);
                        return psUp.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private HoaDon map(ResultSet rs) throws SQLException {
        return new HoaDon(
                rs.getInt("id"),
                rs.getInt("khoan_thu_id"),
                rs.getInt("ho_khau_id"),
                rs.getString("ma_ho_khau"),
                rs.getString("ten_chu_ho"),
                rs.getString("ma_hoa_don"),
                rs.getBigDecimal("tong_tien"),
                rs.getBigDecimal("so_tien_da_nop"),
                rs.getString("trang_thai"),
                rs.getDate("ngay_tao").toLocalDate(),
                rs.getDate("han_nop").toLocalDate()
        );
    }
}
