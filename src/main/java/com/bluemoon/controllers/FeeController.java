package com.bluemoon.controllers;

import com.bluemoon.models.KhoanThu;
import com.bluemoon.services.FeeService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FeeController {

    @FXML private TextField txtMaKhoanThu;
    @FXML private TextField txtTenKhoanThu;
    @FXML private ComboBox<String> cbLoaiPhi;
    @FXML private TextField txtDonGia;
    @FXML private DatePicker dpNgayTao;
    @FXML private TextField txtGhiChu;

    @FXML private TableView<KhoanThu> tableKhoanThu;
    @FXML private TableColumn<KhoanThu, Integer> colId;
    @FXML private TableColumn<KhoanThu, String> colMaKhoanThu;
    @FXML private TableColumn<KhoanThu, String> colTenKhoanThu;
    @FXML private TableColumn<KhoanThu, String> colLoaiPhi;
    @FXML private TableColumn<KhoanThu, BigDecimal> colDonGia;
    @FXML private TableColumn<KhoanThu, LocalDate> colNgayTao;
    @FXML private TableColumn<KhoanThu, String> colGhiChu;

    private FeeService feeService;
    private ObservableList<KhoanThu> feeList;

    @FXML
    public void initialize() {
        feeService = new FeeService();
        feeList = FXCollections.observableArrayList();

        // Setup columns
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colMaKhoanThu.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaKhoanThu()));
        colTenKhoanThu.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTenKhoanThu()));
        colLoaiPhi.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLoaiPhi()));
        colDonGia.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDonGia()));
        colNgayTao.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNgayTao()));
        colGhiChu.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGhiChu()));

        cbLoaiPhi.getSelectionModel().selectFirst();
        
        loadData();
    }

    private void loadData() {
        List<KhoanThu> list = feeService.getAllFees();
        feeList.setAll(list);
        tableKhoanThu.setItems(feeList);
    }

    @FXML
    void handleAdd(ActionEvent event) {
        try {
            String ma = txtMaKhoanThu.getText();
            String ten = txtTenKhoanThu.getText();
            String loai = cbLoaiPhi.getValue();
            String donGiaStr = txtDonGia.getText();
            LocalDate ngayTao = dpNgayTao.getValue();
            String ghiChu = txtGhiChu.getText();

            if (ma == null || ma.isEmpty() || ten == null || ten.isEmpty() || donGiaStr == null || donGiaStr.isEmpty() || ngayTao == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            BigDecimal donGia = new BigDecimal(donGiaStr);

            KhoanThu kt = new KhoanThu(0, ma, ten, loai, donGia, ngayTao, ghiChu);
            boolean success = feeService.createFee(kt);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo đợt thu phí mới thành công.");
                loadData();
                handleClear(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo đợt thu (Có thể trùng mã).");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đơn giá phải là số hợp lệ.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi hệ thống.");
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        txtMaKhoanThu.clear();
        txtTenKhoanThu.clear();
        cbLoaiPhi.getSelectionModel().selectFirst();
        txtDonGia.clear();
        dpNgayTao.setValue(null);
        txtGhiChu.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
