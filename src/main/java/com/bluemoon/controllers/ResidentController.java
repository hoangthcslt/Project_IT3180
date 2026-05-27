package com.bluemoon.controllers;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.ResidentService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ResidentController {

    @FXML
    private TextField txtHoKhauId;
    @FXML
    private TextField txtHoTen;
    @FXML
    private TextField txtCccd;
    @FXML
    private DatePicker dpNgaySinh;
    @FXML
    private TextField txtGioiTinh;
    @FXML
    private TextField txtQuanHe;
    @FXML
    private TextField txtTrangThai;

    @FXML
    private TextField txtSearchHoKhauId;
    @FXML
    private TextField txtSearchHoTen;
    @FXML
    private TextField txtSearchCccd;
    @FXML
    private DatePicker dpSearchNgaySinh;

    @FXML
    private Button btnAddResident;
    @FXML
    private Button btnResetAdd;
    @FXML
    private Button btnSearchResident;
    @FXML
    private Button btnResetSearch;

    @FXML
    private TableView<NhanKhau> tableNhanKhau;
    @FXML
    private TableColumn<NhanKhau, Integer> colId;
    @FXML
    private TableColumn<NhanKhau, Integer> colHoKhauId;
    @FXML
    private TableColumn<NhanKhau, String> colHoTen;
    @FXML
    private TableColumn<NhanKhau, String> colCccd;
    @FXML
    private TableColumn<NhanKhau, LocalDate> colNgaySinh;
    @FXML
    private TableColumn<NhanKhau, String> colGioiTinh;
    @FXML
    private TableColumn<NhanKhau, String> colQuanHe;
    @FXML
    private TableColumn<NhanKhau, String> colTrangThai;
    @FXML
    private TableColumn<NhanKhau, Void> colHoatDong;

    private ResidentService residentService;
    private ObservableList<NhanKhau> residentList;

    @FXML
    public void initialize() {
        residentService = new ResidentService();
        residentList = FXCollections.observableArrayList();

        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colHoKhauId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getHoKhauId()));
        colHoTen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        colCccd.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCccd()));
        colNgaySinh.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNgaySinh()));
        colGioiTinh.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGioiTinh()));
        colQuanHe.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuanHe()));
        colTrangThai.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrangThai()));
        colHoatDong.setCellFactory(col -> actionCell());

        tableNhanKhau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        loadData();

        setupDashboardButton(btnAddResident, "#27ae60", "#219653");
        setupDashboardButton(btnSearchResident, "#27ae60", "#219653");
        setupDashboardButton(btnResetAdd, "#7f8c8d", "#6f7c7d");
        setupDashboardButton(btnResetSearch, "#7f8c8d", "#6f7c7d");
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
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Vui lòng nhập đầy đủ thông tin bắt buộc (ID Hộ khẩu, Họ tên, Ngày sinh).");
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

    @FXML
    void handleSearch(ActionEvent event) {
        try {
            residentList.setAll(residentService.searchResidents(
                    txtSearchHoKhauId.getText(),
                    txtSearchHoTen.getText(),
                    txtSearchCccd.getText(),
                    dpSearchNgaySinh.getValue()));
            tableNhanKhau.setItems(residentList);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "ID Hộ khẩu phải là số hợp lệ.");
        }
    }

    @FXML
    void handleResetSearch(ActionEvent event) {
        txtSearchHoKhauId.clear();
        txtSearchHoTen.clear();
        txtSearchCccd.clear();
        dpSearchNgaySinh.setValue(null);
        loadData();
    }

    private TableCell<NhanKhau, Void> actionCell() {
        return new TableCell<>() {
            private final Button editButton = createActionButton("✎");
            private final Button deleteButton = createActionButton("🗑");
            private final HBox box = new HBox(8, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> confirmDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setAlignment(Pos.CENTER);
            }
        };
    }

    private Button createActionButton(String content) {
        Button button = new Button(content);
        button.setMinSize(34, 30);
        button.setStyle(
                "-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand;");
        button.setOnMouseEntered(event -> button.setStyle(
                "-fx-background-color: #d9e7f5; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand;"));
        button.setOnMouseExited(event -> button.setStyle(
                "-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand;"));
        return button;
    }

    private void setupDashboardButton(Button button, String color, String hoverColor) {
        String baseStyle = "-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: " + hoverColor
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;";
        button.setStyle(baseStyle);
        button.setOnMouseEntered(event -> button.setStyle(hoverStyle));
        button.setOnMouseExited(event -> button.setStyle(baseStyle));
    }

    private void showEditDialog(NhanKhau resident) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa hộ khẩu");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtEditHoKhauId = new TextField(String.valueOf(resident.getHoKhauId()));
        TextField txtEditHoTen = new TextField(resident.getHoTen());
        TextField txtEditCccd = new TextField(resident.getCccd());
        DatePicker dpEditNgaySinh = new DatePicker(resident.getNgaySinh());
        TextField txtEditGioiTinh = new TextField(resident.getGioiTinh());
        TextField txtEditQuanHe = new TextField(resident.getQuanHe());
        TextField txtEditTrangThai = new TextField(resident.getTrangThai());

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(18));
        form.add(new Label("ID Hộ khẩu"), 0, 0);
        form.add(txtEditHoKhauId, 1, 0);
        form.add(new Label("Họ tên"), 0, 1);
        form.add(txtEditHoTen, 1, 1);
        form.add(new Label("CCCD"), 0, 2);
        form.add(txtEditCccd, 1, 2);
        form.add(new Label("Ngày sinh"), 0, 3);
        form.add(dpEditNgaySinh, 1, 3);
        form.add(new Label("Giới tính"), 0, 4);
        form.add(txtEditGioiTinh, 1, 4);
        form.add(new Label("Quan hệ"), 0, 5);
        form.add(txtEditQuanHe, 1, 5);
        form.add(new Label("Trạng thái"), 0, 6);
        form.add(txtEditTrangThai, 1, 6);
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (txtEditHoKhauId.getText().isBlank() || txtEditHoTen.getText().isBlank()
                        || dpEditNgaySinh.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin bắt buộc.");
                    return;
                }
                int hoKhauId = Integer.parseInt(txtEditHoKhauId.getText());
                NhanKhau updated = new NhanKhau(
                        resident.getId(),
                        hoKhauId,
                        txtEditHoTen.getText(),
                        txtEditCccd.getText(),
                        dpEditNgaySinh.getValue(),
                        txtEditGioiTinh.getText(),
                        txtEditQuanHe.getText(),
                        txtEditTrangThai.getText());
                if (residentService.updateResident(updated)) {
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật nhân khẩu.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "ID Hộ khẩu phải là số hợp lệ.");
            }
        }
    }

    private void confirmDelete(NhanKhau resident) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa không?");
        ButtonType yes = new ButtonType("Có");
        ButtonType no = new ButtonType("Không");
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            if (residentService.deleteResident(resident.getId())) {
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa nhân khẩu.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
