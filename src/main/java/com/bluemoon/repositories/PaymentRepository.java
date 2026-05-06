package com.bluemoon.repositories;

import com.bluemoon.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

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
}
