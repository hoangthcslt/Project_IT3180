package com.bluemoon.controllers;

import com.bluemoon.models.HoKhau;
import com.bluemoon.models.KhoanThu;
import com.bluemoon.repositories.PaymentRepository;
import com.bluemoon.services.FeeService;
import com.bluemoon.services.HouseholdService;
import com.bluemoon.services.PaymentService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PaymentController {

    @FXML private TextField txtMaHoKhau;
    @FXML private Label lblTenChuHo;
    @FXML private ComboBox<KhoanThu> cbKhoanThu;
    @FXML private Label lblNoHienTai;
    @FXML private TextField txtSoTienNop;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private TextField txtNguoiNop;

    private PaymentService paymentService = new PaymentService();
    private PaymentRepository paymentRepository = new PaymentRepository();
    private HouseholdService householdService = new HouseholdService();
    private FeeService feeService = new FeeService();

    private HoKhau currentHoKhau = null;

    @FXML
    public void initialize() {
        // Setup cbHinhThuc
        cbHinhThuc.setItems(FXCollections.observableArrayList("TIEN_MAT", "CHUYEN_KHOAN"));
        cbHinhThuc.getSelectionModel().selectFirst();

        // Setup cbKhoanThu
        List<KhoanThu> khoanThuList = feeService.getAllFees();
        cbKhoanThu.setItems(FXCollections.observableArrayList(khoanThuList));
        cbKhoanThu.setConverter(new StringConverter<KhoanThu>() {
            @Override
            public String toString(KhoanThu object) {
                return object == null ? null : object.getTenKhoanThu();
            }

            @Override
            public KhoanThu fromString(String string) {
                return null; // Not needed
            }
        });

        // Add listener when selecting a fee to recalculate debt
        cbKhoanThu.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateDebt();
        });

        // Setup text formatter for numbers
        txtSoTienNop.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));
    }

    @FXML
    public void handleTimHoKhau(ActionEvent event) {
        String maHoKhau = txtMaHoKhau.getText();
        if (maHoKhau == null || maHoKhau.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mã hộ khẩu.");
            return;
        }

        HoKhau hk = householdService.findByMaHoKhau(maHoKhau);
        if (hk != null) {
            currentHoKhau = hk;
            lblTenChuHo.setText(hk.getTenChuHo());
            updateDebt();
        } else {
            currentHoKhau = null;
            lblTenChuHo.setText("---");
            lblNoHienTai.setText("0");
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy hộ khẩu nào với mã này.");
        }
    }

    private void updateDebt() {
        if (currentHoKhau != null && cbKhoanThu.getValue() != null) {
            BigDecimal debt = feeService.calculateDebt(currentHoKhau.getId(), cbKhoanThu.getValue().getId());
            lblNoHienTai.setText(debt.toString());
        } else {
            lblNoHienTai.setText("0");
        }
    }

    @FXML
    public void handleThanhToan(ActionEvent event) {
        try {
            if (currentHoKhau == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng tìm và chọn hộ khẩu.");
                return;
            }
            if (cbKhoanThu.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khoản thu.");
                return;
            }

            BigDecimal noHienTai = new BigDecimal(lblNoHienTai.getText());
            BigDecimal soTien = paymentService.validateTienNop(txtSoTienNop.getText(), noHienTai);
            
            int hoKhauId = currentHoKhau.getId();
            int khoanThuId = cbKhoanThu.getValue().getId();
            String hinhThuc = cbHinhThuc.getValue() != null ? cbHinhThuc.getValue() : "TIEN_MAT";
            String nguoiNop = txtNguoiNop.getText();
            
            if (nguoiNop == null || nguoiNop.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng nhập tên người nộp.");
                return;
            }

            boolean success = paymentRepository.thucHienThanhToan(hoKhauId, khoanThuId, soTien, hinhThuc, nguoiNop);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán thành công!");
                txtSoTienNop.clear();
                updateDebt(); // Recalculate debt
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
