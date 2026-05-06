package com.bluemoon.controllers;

import com.bluemoon.repositories.PaymentRepository;
import com.bluemoon.services.PaymentService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.math.BigDecimal;
import java.sql.SQLException;

public class PaymentController {

    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cbKhoanThu;
    @FXML private Label lblNoHienTai;
    @FXML private TextField txtSoTienNop;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private TextField txtNguoiNop;

    private PaymentService paymentService = new PaymentService();
    private PaymentRepository paymentRepository = new PaymentRepository();

    @FXML
    public void handleThanhToan(ActionEvent event) {
        try {
            // Giả lập lấy thông tin nợ còn lại (vì bị giới hạn không viết thêm hàm repo)
            BigDecimal noHienTai = new BigDecimal(lblNoHienTai.getText());
            
            // Validate tiền nộp
            BigDecimal soTien = paymentService.validateTienNop(txtSoTienNop.getText(), noHienTai);
            
            // Lấy các trường thông tin giả lập (Id hộ khẩu, Id khoản thu)
            int hoKhauId = 1; // dummy data
            int khoanThuId = 1; // dummy data
            String hinhThuc = cbHinhThuc.getValue() != null ? cbHinhThuc.getValue() : "TIEN_MAT";
            String nguoiNop = txtNguoiNop.getText();
            
            if (nguoiNop == null || nguoiNop.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng nhập tên người nộp");
                return;
            }

            boolean success = paymentRepository.thucHienThanhToan(hoKhauId, khoanThuId, soTien, hinhThuc, nguoiNop);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán thành công!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi khi lưu giao dịch.");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xác thực", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
