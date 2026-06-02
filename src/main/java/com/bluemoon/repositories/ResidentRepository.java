package com.bluemoon.repositories;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResidentRepository {
    private static volatile boolean schemaReady;

    public List<NhanKhau> findByHoKhauId(int hoKhauId) {
        ensureExtendedSchema();
        return query("SELECT * FROM nhan_khau WHERE ho_khau_id = ? ORDER BY id DESC", List.of(hoKhauId));
    }

    public List<NhanKhau> findAll() {
        ensureExtendedSchema();
        return query("SELECT * FROM nhan_khau ORDER BY id DESC", List.of());
    }

    public boolean insert(NhanKhau resident) {
        ensureExtendedSchema();
        String sql = "INSERT INTO nhan_khau (ho_khau_id, ho_ten, cccd, so_dien_thoai, ngay_sinh, gioi_tinh, quan_he, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setFields(pstmt, resident);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(NhanKhau resident) {
        ensureExtendedSchema();
        String sql = "UPDATE nhan_khau SET ho_khau_id = ?, ho_ten = ?, cccd = ?, so_dien_thoai = ?, ngay_sinh = ?, gioi_tinh = ?, quan_he = ?, trang_thai = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setFields(pstmt, resident);
            pstmt.setInt(9, resident.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM nhan_khau WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasHouseholdHead(int householdId, Integer excludedResidentId) {
        String sql = "SELECT COUNT(*) FROM nhan_khau WHERE ho_khau_id = ? AND quan_he = 'Chủ hộ'"
                + (excludedResidentId == null ? "" : " AND id <> ?");
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, householdId);
            if (excludedResidentId != null) pstmt.setInt(2, excludedResidentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<NhanKhau> search(Integer householdId, String name, String cccd, String phone, LocalDate birthday,
            String gender, String relationship, String status) {
        ensureExtendedSchema();
        StringBuilder sql = new StringBuilder("SELECT * FROM nhan_khau WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (householdId != null) { sql.append(" AND ho_khau_id = ?"); params.add(householdId); }
        addLike(sql, params, "ho_ten", name);
        addLike(sql, params, "cccd", cccd);
        addLike(sql, params, "so_dien_thoai", phone);
        if (birthday != null) { sql.append(" AND ngay_sinh = ?"); params.add(Date.valueOf(birthday)); }
        addExact(sql, params, "gioi_tinh", hasText(gender) ? genderCode(gender) : gender);
        addExact(sql, params, "quan_he", relationship);
        addExact(sql, params, "trang_thai", hasText(status) ? statusCode(status) : status);
        sql.append(" ORDER BY id DESC");
        return query(sql.toString(), params);
    }

    private List<NhanKhau> query(String sql, List<Object> params) {
        List<NhanKhau> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private NhanKhau map(ResultSet rs) throws SQLException {
        Date birthday = rs.getDate("ngay_sinh");
        return new NhanKhau(rs.getInt("id"), rs.getInt("ho_khau_id"), rs.getString("ho_ten"),
                rs.getString("cccd"), rs.getString("so_dien_thoai"),
                birthday == null ? null : birthday.toLocalDate(), genderLabel(rs.getString("gioi_tinh")),
                rs.getString("quan_he"), statusLabel(rs.getString("trang_thai")));
    }

    private void setFields(PreparedStatement pstmt, NhanKhau resident) throws SQLException {
        pstmt.setInt(1, resident.getHoKhauId());
        pstmt.setString(2, resident.getHoTen());
        pstmt.setString(3, emptyToNull(resident.getCccd()));
        pstmt.setString(4, emptyToNull(resident.getSoDienThoai()));
        pstmt.setDate(5, Date.valueOf(resident.getNgaySinh()));
        pstmt.setString(6, genderCode(resident.getGioiTinh()));
        pstmt.setString(7, resident.getQuanHe());
        pstmt.setString(8, statusCode(resident.getTrangThai()));
    }

    private void addLike(StringBuilder sql, List<Object> params, String column, String value) {
        if (hasText(value)) { sql.append(" AND ").append(column).append(" LIKE ?"); params.add("%" + value.trim() + "%"); }
    }

    private void addExact(StringBuilder sql, List<Object> params, String column, String value) {
        if (hasText(value)) { sql.append(" AND ").append(column).append(" = ?"); params.add(value.trim()); }
    }

    private String emptyToNull(String value) { return hasText(value) ? value.trim() : null; }
    private boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }

    private String genderCode(String value) {
        return switch (value) {
            case "Nam" -> "NAM";
            case "Nữ" -> "NU";
            default -> value;
        };
    }

    private String genderLabel(String value) {
        return switch (value) {
            case "NAM" -> "Nam";
            case "NU" -> "Nữ";
            default -> value;
        };
    }

    private String statusCode(String value) {
        return switch (value) {
            case "Thường trú" -> "THUONG_TRU";
            case "Tạm trú" -> "TAM_TRU";
            case "Tạm vắng" -> "TAM_VANG";
            default -> value;
        };
    }

    private String statusLabel(String value) {
        return switch (value) {
            case "THUONG_TRU" -> "Thường trú";
            case "TAM_TRU" -> "Tạm trú";
            case "TAM_VANG" -> "Tạm vắng";
            default -> value;
        };
    }

    private synchronized void ensureExtendedSchema() {
        if (schemaReady) return;
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet columns = metaData.getColumns(conn.getCatalog(), null, "nhan_khau", "so_dien_thoai")) {
                if (!columns.next()) {
                    try (Statement statement = conn.createStatement()) {
                        statement.executeUpdate("ALTER TABLE nhan_khau ADD COLUMN so_dien_thoai VARCHAR(15) NULL AFTER cccd");
                    }
                }
            }
            schemaReady = true;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể mở rộng bảng nhan_khau mà vẫn giữ dữ liệu hiện có.", e);
        }
    }
}
