package com.bluemoon.repositories;

import com.bluemoon.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThongKeRepository {

    public List<Map<String, Object>> layDuLieuThongKe() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT kt.ten_khoan_thu, SUM(nt.so_tien_nop) as tong_da_thu " +
                     "FROM khoan_thu kt " +
                     "LEFT JOIN nop_tien nt ON kt.id = nt.khoan_thu_id " +
                     "GROUP BY kt.id, kt.ten_khoan_thu";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("tenKhoanThu", rs.getString("ten_khoan_thu"));
                row.put("tongDaThu", rs.getBigDecimal("tong_da_thu"));
                result.add(row);
            }
        }
        return result;
    }

    public Map<String, Object> layThongKeTongQuan() throws SQLException {
        Map<String, Object> result = new HashMap<>();
        String sqlNhanKhau = "SELECT COUNT(*) FROM nhan_khau";
        String sqlHoKhau = "SELECT COUNT(*) FROM ho_khau";
        String sqlDoanhThu = "SELECT COALESCE(SUM(so_tien_nop), 0) FROM nop_tien";
        String sqlNo = "SELECT COALESCE(SUM(hd.tong_tien - hd.so_tien_da_nop), 0) " +
                       "FROM hoa_don hd " +
                       "JOIN khoan_thu kt ON hd.khoan_thu_id = kt.id " +
                       "WHERE hd.trang_thai = 'CHUA_NOP' AND kt.trang_thai = 'PUBLISHED' AND kt.loai_phi = 'BAT_BUOC'";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlNhanKhau); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result.put("tongCuDan", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlHoKhau); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result.put("tongCanHo", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDoanhThu); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result.put("tongDoanhThu", rs.getBigDecimal(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlNo); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal no = rs.getBigDecimal(1);
                    result.put("tongNo", no == null ? BigDecimal.ZERO : no);
                } else {
                    result.put("tongNo", BigDecimal.ZERO);
                }
            }
        }
        return result;
    }

    public List<Map<String, Object>> layThongKeGioiTinh() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT gioi_tinh, COUNT(*) as count FROM nhan_khau GROUP BY gioi_tinh";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("gioiTinh", rs.getString("gioi_tinh"));
                row.put("count", rs.getInt("count"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> layThongKeTrangThai() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT trang_thai, COUNT(*) as count FROM nhan_khau GROUP BY trang_thai";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("trangThai", rs.getString("trang_thai"));
                row.put("count", rs.getInt("count"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> layThongKeNoVaThu() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT " +
                     "  kt.id, " +
                     "  kt.ten_khoan_thu, " +
                     "  COALESCE(SUM(hd.so_tien_da_nop), 0) AS tong_da_nop, " +
                     "  COALESCE(SUM(CASE WHEN hd.trang_thai = 'CHUA_NOP' AND kt.loai_phi = 'BAT_BUOC' THEN (hd.tong_tien - hd.so_tien_da_nop) ELSE 0 END), 0) AS tong_chua_nop " +
                     "FROM khoan_thu kt " +
                     "LEFT JOIN hoa_don hd ON hd.khoan_thu_id = kt.id " +
                     "GROUP BY kt.id, kt.ten_khoan_thu";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("tenKhoanThu", rs.getString("ten_khoan_thu"));
                row.put("tongDaNop", rs.getBigDecimal("tong_da_nop"));
                row.put("tongChuaNop", rs.getBigDecimal("tong_chua_nop"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> layDoanhThuTheoThoiGian(String kieuThoiGian, Integer namLoc) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        
        if ("YEAR".equalsIgnoreCase(kieuThoiGian)) {
            sql.append("SELECT CAST(YEAR(ngay_nop) AS CHAR) as label, SUM(so_tien_nop) as val ");
            sql.append("FROM nop_tien ");
            if (namLoc != null) {
                sql.append("WHERE YEAR(ngay_nop) = ? ");
            }
            sql.append("GROUP BY label ORDER BY label");
        } else if ("MONTH".equalsIgnoreCase(kieuThoiGian)) {
            sql.append("SELECT DATE_FORMAT(ngay_nop, '%Y-%m') as label, SUM(so_tien_nop) as val ");
            sql.append("FROM nop_tien ");
            if (namLoc != null) {
                sql.append("WHERE YEAR(ngay_nop) = ? ");
            }
            sql.append("GROUP BY label ORDER BY label");
        } else if ("WEEK".equalsIgnoreCase(kieuThoiGian)) {
            sql.append("SELECT DATE_FORMAT(ngay_nop, '%Y-W%u') as label, SUM(so_tien_nop) as val ");
            sql.append("FROM nop_tien ");
            if (namLoc != null) {
                sql.append("WHERE YEAR(ngay_nop) = ? ");
            }
            sql.append("GROUP BY label ORDER BY label");
        } else {
            return result;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            if (namLoc != null) {
                pstmt.setInt(1, namLoc);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("label", rs.getString("label"));
                    row.put("val", rs.getBigDecimal("val"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    public List<Map<String, Object>> layThongKeTyLeCanHo() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT status, COUNT(*) as count FROM ho_khau GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("status", rs.getString("status"));
                row.put("count", rs.getInt("count"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> layDanCuTheoThoiGian() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT hk.ngay_lap, COUNT(nk.id) as count " +
                     "FROM nhan_khau nk " +
                     "JOIN ho_khau hk ON nk.ho_khau_id = hk.id " +
                     "GROUP BY hk.ngay_lap " +
                     "ORDER BY hk.ngay_lap ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ngayLap", rs.getDate("ngay_lap").toLocalDate());
                row.put("count", rs.getInt("count"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> layDoanhThuTheoThang() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(ngay_nop, '%Y-%m') as label, SUM(so_tien_nop) as val " +
                     "FROM nop_tien " +
                     "GROUP BY label " +
                     "ORDER BY label ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("label", rs.getString("label"));
                row.put("val", rs.getBigDecimal("val"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> layNoTheoThang() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT " +
                     "  DATE_FORMAT(kt.ngay_tao, '%Y-%m') AS label, " +
                     "  SUM(GREATEST(hk.dien_tich * kt.don_gia - COALESCE(paid_sub.tong_nop, 0), 0)) AS val " +
                     "FROM khoan_thu kt " +
                     "CROSS JOIN ho_khau hk " +
                     "LEFT JOIN (" +
                     "  SELECT ho_khau_id, khoan_thu_id, SUM(so_tien_nop) AS tong_nop " +
                     "  FROM nop_tien " +
                     "  GROUP BY ho_khau_id, khoan_thu_id" +
                     ") paid_sub ON paid_sub.ho_khau_id = hk.id AND paid_sub.khoan_thu_id = kt.id " +
                     "GROUP BY label " +
                     "ORDER BY label ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("label", rs.getString("label"));
                row.put("val", rs.getBigDecimal("val"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Integer> layDanhSachNamCoGiaoDich() throws SQLException {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(ngay_nop) as nam FROM nop_tien ORDER BY nam DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int yr = rs.getInt("nam");
                if (yr > 0) years.add(yr);
            }
        }
        return years;
    }
}
