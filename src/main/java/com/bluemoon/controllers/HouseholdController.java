package com.bluemoon.controllers;

import com.bluemoon.models.HoKhau;
import com.bluemoon.services.HouseholdService;
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

public class HouseholdController {

    @FXML private TextField txtMaHoKhau;
    @FXML private TextField txtTenChuHo;
    @FXML private TextField txtDienTich;
    @FXML private DatePicker dpNgayLap;

    @FXML private TableView<HoKhau> tableHoKhau;
    @FXML private TableColumn<HoKhau, Integer> colId;
    @FXML private TableColumn<HoKhau, String> colMaHoKhau;
    @FXML private TableColumn<HoKhau, String> colTenChuHo;
    @FXML private TableColumn<HoKhau, BigDecimal> colDienTich;
    @FXML private TableColumn<HoKhau, LocalDate> colNgayLap;

    private HouseholdService householdService;
    private ObservableList<HoKhau> householdList;

    @FXML
    public void initialize() {
        householdService = new HouseholdService();
        householdList = FXCollections.observableArrayList();

        // Setup columns
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colMaHoKhau.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaHoKhau()));
        colTenChuHo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTenChuHo()));
        colDienTich.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDienTich()));
        colNgayLap.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNgayLap()));

        loadData();
    }

    private void loadData() {
        List<HoKhau> list = householdService.getAllHouseholds();
        householdList.setAll(list);
        tableHoKhau.setItems(householdList);
    }

    @FXML
    void handleAdd(ActionEvent event) {
        try {
            String ma = txtMaHoKhau.getText();
            String ten = txtTenChuHo.getText();
            String dienTichStr = txtDienTich.getText();
            LocalDate ngayLap = dpNgayLap.getValue();

            if (ma == null || ma.isEmpty() || ten == null || ten.isEmpty() || dienTichStr == null || dienTichStr.isEmpty() || ngayLap == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            BigDecimal dienTich = new BigDecimal(dienTichStr);

            HoKhau hk = new HoKhau(0, ma, ten, dienTich, ngayLap);
            boolean success = householdService.addHousehold(hk);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm hộ khẩu mới thành công.");
                loadData();
                handleClear(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm hộ khẩu (Có thể trùng mã).");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Diện tích phải là số hợp lệ.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi hệ thống.");
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        txtMaHoKhau.clear();
        txtTenChuHo.clear();
        txtDienTich.clear();
        dpNgayLap.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
