package com.bluemoon.repositories;

import com.bluemoon.models.HoKhau;
import com.bluemoon.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HouseholdRepository {
    private static volatile boolean schemaReady;

    public List<HoKhau> findAll() {
        ensureExtendedSchema();
        return query("SELECT * FROM ho_khau ORDER BY id DESC", List.of());
    }

    public boolean isMaHoKhauExists(String maHoKhau) {
        return isMaHoKhauExists(maHoKhau, null);
    }

    public boolean isMaHoKhauExists(String maHoKhau, Integer excludedId) {
        ensureExtendedSchema();
        String sql = "SELECT COUNT(*) FROM ho_khau WHERE ma_ho_khau = ?" + (excludedId == null ? "" : " AND id <> ?");
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoKhau);
            if (excludedId != null) pstmt.setInt(2, excludedId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public HoKhau findByMaHoKhau(String maHoKhau) {
        ensureExtendedSchema();
        List<HoKhau> result = query("SELECT * FROM ho_khau WHERE ma_ho_khau = ?", List.of(maHoKhau));
        return result.isEmpty() ? null : result.get(0);
    }

    public boolean insert(HoKhau hoKhau) {
        ensureExtendedSchema();
        String sql = "INSERT INTO ho_khau (ma_ho_khau, ten_chu_ho, dien_tich, status, so_nguoi, phuong_tien, ngay_lap, so_xe_may, so_oto) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setFields(pstmt, hoKhau);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(HoKhau hoKhau) {
        ensureExtendedSchema();
        String sql = "UPDATE ho_khau SET ma_ho_khau = ?, ten_chu_ho = ?, dien_tich = ?, status = ?, so_nguoi = ?, phuong_tien = ?, ngay_lap = ?, so_xe_may = ?, so_oto = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setFields(pstmt, hoKhau);
            pstmt.setInt(10, hoKhau.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        ensureExtendedSchema();
        String deletePaymentsSql = "DELETE FROM nop_tien WHERE ho_khau_id = ?";
        String deleteHouseholdSql = "DELETE FROM ho_khau WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                System.out.println("Deleting ho_khau id: " + id);
                printHouseholdById(conn, id);
                printReferenceCount(conn, "nhan_khau", "ho_khau_id", id);
                printReferenceCount(conn, "nop_tien", "ho_khau_id", id);
                printReferenceCount(conn, "hoa_don", "ho_khau_id", id);

                int paymentRows;
                try (PreparedStatement pstmt = conn.prepareStatement(deletePaymentsSql)) {
                    pstmt.setInt(1, id);
                    paymentRows = pstmt.executeUpdate();
                }
                System.out.println("nop_tien rows deleted: " + paymentRows);

                int rows;
                try (PreparedStatement pstmt = conn.prepareStatement(deleteHouseholdSql)) {
                    pstmt.setInt(1, id);
                    rows = pstmt.executeUpdate();
                }
                System.out.println("Rows affected: " + rows);

                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the xoa ho khau id=" + id + ": " + e.getMessage(), e);
        }
    }

    public List<HoKhau> search(String ma, String ten, String dienTich, String trangThai, String soNguoi,
            String phuongTien) {
        ensureExtendedSchema();
        StringBuilder sql = new StringBuilder("SELECT * FROM ho_khau WHERE 1=1");
        List<Object> params = new ArrayList<>();
        addLike(sql, params, "ma_ho_khau", ma);
        addLike(sql, params, "ten_chu_ho", ten);
        if (hasText(dienTich)) {
            sql.append(" AND dien_tich = ?");
            params.add(new java.math.BigDecimal(dienTich.trim()));
        }
        if (hasText(trangThai)) {
            sql.append(" AND status = ?");
            params.add(trangThai.trim());
        }
        if (hasText(soNguoi)) {
            sql.append(" AND so_nguoi = ?");
            params.add(Integer.parseInt(soNguoi.trim()));
        }
        addLike(sql, params, "phuong_tien", phuongTien);
        sql.append(" ORDER BY id DESC");
        return query(sql.toString(), params);
    }

    private List<HoKhau> query(String sql, List<Object> params) {
        List<HoKhau> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private HoKhau map(ResultSet rs) throws SQLException {
        return new HoKhau(rs.getInt("id"), rs.getString("ma_ho_khau"), rs.getString("ten_chu_ho"),
                rs.getBigDecimal("dien_tich"), rs.getString("status"), rs.getInt("so_nguoi"),
                rs.getString("phuong_tien"), rs.getDate("ngay_lap").toLocalDate(),
                rs.getInt("so_xe_may"), rs.getInt("so_oto"));
    }

    private void setFields(PreparedStatement pstmt, HoKhau hoKhau) throws SQLException {
        pstmt.setString(1, hoKhau.getMaHoKhau());
        pstmt.setString(2, hoKhau.getTenChuHo());
        pstmt.setBigDecimal(3, hoKhau.getDienTich());
        pstmt.setString(4, hoKhau.getTrangThai());
        pstmt.setInt(5, hoKhau.getSoNguoi());
        pstmt.setString(6, hoKhau.getPhuongTien());
        pstmt.setDate(7, Date.valueOf(hoKhau.getNgayLap()));
        pstmt.setInt(8, hoKhau.getSoXeMay());
        pstmt.setInt(9, hoKhau.getSoOto());
    }

    private void addLike(StringBuilder sql, List<Object> params, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            params.add("%" + value.trim() + "%");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private synchronized void ensureExtendedSchema() {
        if (schemaReady) return;
        try (Connection conn = DBConnection.getConnection()) {
            boolean addedPeopleColumn = false;
            if (!hasColumn(conn, "status")) {
                execute(conn, "ALTER TABLE ho_khau ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'Đang ở' AFTER dien_tich");
            }
            if (!hasColumn(conn, "so_nguoi")) {
                execute(conn, "ALTER TABLE ho_khau ADD COLUMN so_nguoi INT NOT NULL DEFAULT 0 AFTER status");
                addedPeopleColumn = true;
            }
            if (!hasColumn(conn, "phuong_tien")) {
                execute(conn, "ALTER TABLE ho_khau ADD COLUMN phuong_tien VARCHAR(255) NOT NULL DEFAULT 'Chưa cập nhật' AFTER so_nguoi");
            }
            if (!hasColumn(conn, "so_xe_may")) {
                execute(conn, "ALTER TABLE ho_khau ADD COLUMN so_xe_may INT NOT NULL DEFAULT 0 AFTER ngay_lap");
            }
            if (!hasColumn(conn, "so_oto")) {
                execute(conn, "ALTER TABLE ho_khau ADD COLUMN so_oto INT NOT NULL DEFAULT 0 AFTER so_xe_may");
            }
            if (addedPeopleColumn) {
                execute(conn, "UPDATE ho_khau hk LEFT JOIN (SELECT ho_khau_id, COUNT(*) AS total FROM nhan_khau GROUP BY ho_khau_id) nk ON nk.ho_khau_id = hk.id SET hk.so_nguoi = COALESCE(nk.total, 0)");
            }
            schemaReady = true;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể mở rộng bảng ho_khau mà vẫn giữ dữ liệu hiện có.", e);
        }
    }

    private boolean hasColumn(Connection conn, String column) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet columns = metaData.getColumns(conn.getCatalog(), null, "ho_khau", column)) {
            return columns.next();
        }
    }

    private void execute(Connection conn, String sql) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void printHouseholdById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM ho_khau WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Found ho_khau: id=" + rs.getInt("id")
                            + ", ma_ho_khau=" + rs.getString("ma_ho_khau"));
                } else {
                    System.out.println("Found ho_khau: no row for id=" + id);
                }
            }
        }
    }

    private void printReferenceCount(Connection conn, String tableName, String columnName, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println(tableName + " references for ho_khau id=" + id + ": " + rs.getInt(1));
                }
            }
        }
    }
}
