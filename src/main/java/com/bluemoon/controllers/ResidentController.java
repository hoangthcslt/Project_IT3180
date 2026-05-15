package com.bluemoon.controllers;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.ResidentService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

public class ResidentController {

    @FXML private TextField txtHoKhauId;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtCccd;
    @FXML private DatePicker dpNgaySinh;
    @FXML private TextField txtGioiTinh;
    @FXML private TextField txtQuanHe;
    @FXML private TextField txtTrangThai;

    @FXML private TableView<NhanKhau> tableNhanKhau;
    @FXML private TableColumn<NhanKhau, Integer> colId;
    @FXML private TableColumn<NhanKhau, Integer> colHoKhauId;
    @FXML private TableColumn<NhanKhau, String> colHoTen;
    @FXML private TableColumn<NhanKhau, String> colCccd;
    @FXML private TableColumn<NhanKhau, LocalDate> colNgaySinh;
    @FXML private TableColumn<NhanKhau, String> colGioiTinh;
    @FXML private TableColumn<NhanKhau, String> colQuanHe;
    @FXML private TableColumn<NhanKhau, String> colTrangThai;

    private ResidentService residentService;
    private ObservableList<NhanKhau> residentList;

    @FXML
    public void initialize() {
        residentService = new ResidentService();
        residentList = FXCollections.observableArrayList();

        // Setup columns
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colHoKhauId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getHoKhauId()));
        colHoTen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        colCccd.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCccd()));
        colNgaySinh.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNgaySinh()));
        colGioiTinh.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGioiTinh()));
        colQuanHe.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuanHe()));
        colTrangThai.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrangThai()));

        loadData();
    }

    private void loadData() {
        List<NhanKhau> list = residentService.getAllResidents();
        residentList.setAll(list);
        tableNhanKhau.setItems(residentList);
    }

    @FXML
    void handleAdd(ActionEvent event) {
        try {
            String hoKhauIdStr = txtHoKhauId.getText();
            String hoTen = txtHoTen.getText();
            String cccd = txtCccd.getText();
            LocalDate ngaySinh = dpNgaySinh.getValue();
            String gioiTinh = txtGioiTinh.getText();
            String quanHe = txtQuanHe.getText();
            String trangThai = txtTrangThai.getText();

            if (hoKhauIdStr == null || hoKhauIdStr.isEmpty() || hoTen == null || hoTen.isEmpty() || ngaySinh == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin bắt buộc (ID Hộ khẩu, Họ tên, Ngày sinh).");
                return;
            }

            int hoKhauId = Integer.parseInt(hoKhauIdStr);

            NhanKhau nk = new NhanKhau(0, hoKhauId, hoTen, cccd, ngaySinh, gioiTinh, quanHe, trangThai);
            boolean success = residentService.addResident(nk);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm nhân khẩu mới thành công.");
                loadData();
                handleClear(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm nhân khẩu (Kiểm tra lại ID Hộ khẩu).");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "ID Hộ khẩu phải là số hợp lệ.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi hệ thống.");
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        txtHoKhauId.clear();
        txtHoTen.clear();
        txtCccd.clear();
        dpNgaySinh.setValue(null);
        txtGioiTinh.clear();
        txtQuanHe.clear();
        txtTrangThai.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
