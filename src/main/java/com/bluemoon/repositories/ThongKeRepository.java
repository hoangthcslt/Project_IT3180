package com.bluemoon.repositories;

import com.bluemoon.utils.DBConnection;
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
        // Giả lập dư nợ bằng cách query tổng diện tích x đơn giá (hoặc mặc định) và trừ đi số đã thu.
        // Tuy nhiên theo prompt, chỉ cần viết câu lệnh SQL dùng SUM() và GROUP BY
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
                // Nợ có thể tính thêm nếu có schema phức tạp, tạm thời lấy những gì sum được
                result.add(row);
            }
        }
        return result;
    }
}
