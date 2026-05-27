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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FeeController {

    @FXML
    private TextField txtMaKhoanThu;
    @FXML
    private TextField txtTenKhoanThu;
    @FXML
    private ComboBox<String> cbLoaiPhi;
    @FXML
    private TextField txtDonGia;
    @FXML
    private DatePicker dpNgayTao;
    @FXML
    private TextField txtGhiChu;

    @FXML
    private TableView<KhoanThu> tableKhoanThu;
    @FXML
    private TableColumn<KhoanThu, Integer> colId;
    @FXML
    private TableColumn<KhoanThu, String> colMaKhoanThu;
    @FXML
    private TableColumn<KhoanThu, String> colTenKhoanThu;
    @FXML
    private TableColumn<KhoanThu, String> colLoaiPhi;
    @FXML
    private TableColumn<KhoanThu, BigDecimal> colDonGia;
    @FXML
    private TableColumn<KhoanThu, LocalDate> colNgayTao;
    @FXML
    private TableColumn<KhoanThu, String> colGhiChu;
    @FXML
    private TableColumn<KhoanThu, Void> colActions;

    @FXML
    private TextField txtSearchMa;
    @FXML
    private TextField txtSearchTen;
    @FXML
    private ComboBox<String> cbSearchLoai;
    @FXML
    private TextField txtSearchDonGia;
    @FXML
    private DatePicker dpSearchNgayTao;
    @FXML
    private Button btnSearchFee;
    @FXML
    private Button btnResetSearch;

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
        cbSearchLoai.getSelectionModel().selectFirst();

        loadData();

        // Validation for Đơn giá: only allow numbers and decimal points
        txtDonGia.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));

        // Actions column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Xóa");
            private final HBox box = new HBox(8, editButton, deleteButton);

            {
                editButton.setStyle(
                        "-fx-background-color: #2f80ed; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 12;");
                deleteButton.setStyle(
                        "-fx-background-color: #eb5757; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 12;");
                editButton.setOnAction(event -> {
                    KhoanThu item = getTableView().getItems().get(getIndex());
                    showEditDialog(item);
                });
                deleteButton.setOnAction(event -> {
                    KhoanThu item = getTableView().getItems().get(getIndex());
                    handleDelete(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
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

            if (ma == null || ma.isEmpty() || ten == null || ten.isEmpty() || donGiaStr == null || donGiaStr.isEmpty()
                    || ngayTao == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            if (feeService.isMaKhoanThuExists(ma)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mã khoản thu này đã tồn tại trong hệ thống!");
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
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo đợt thu.");
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

    @FXML
    void handleSearch(ActionEvent event) {
        String ma = txtSearchMa.getText();
        String ten = txtSearchTen.getText();
        String loai = cbSearchLoai.getValue();
        String donGia = txtSearchDonGia.getText();
        LocalDate ngay = dpSearchNgayTao.getValue();
        List<KhoanThu> result = feeService.searchFees(ma, ten, loai == null || loai.isEmpty() ? null : loai, donGia,
                ngay);
        feeList.setAll(result);
        tableKhoanThu.setItems(feeList);
    }

    @FXML
    void handleResetSearch(ActionEvent event) {
        txtSearchMa.clear();
        txtSearchTen.clear();
        cbSearchLoai.getSelectionModel().selectFirst();
        txtSearchDonGia.clear();
        dpSearchNgayTao.setValue(null);
        loadData();
    }

    private void showEditDialog(KhoanThu item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa khoản thu");
        dialog.setHeaderText(null);

        ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));

        TextField editMa = new TextField(item.getMaKhoanThu());
        TextField editTen = new TextField(item.getTenKhoanThu());
        ComboBox<String> editLoai = new ComboBox<>(FXCollections.observableArrayList("BAT_BUOC", "TU_NGUYEN"));
        editLoai.setValue(item.getLoaiPhi());
        TextField editDonGia = new TextField(item.getDonGia() != null ? item.getDonGia().toString() : "");
        DatePicker editNgay = new DatePicker(item.getNgayTao());
        TextField editGhiChu = new TextField(item.getGhiChu());

        grid.add(new Label("Mã khoản thu"), 0, 0);
        grid.add(editMa, 1, 0);
        grid.add(new Label("Tên khoản thu"), 0, 1);
        grid.add(editTen, 1, 1);
        grid.add(new Label("Loại phí"), 0, 2);
        grid.add(editLoai, 1, 2);
        grid.add(new Label("Đơn giá"), 0, 3);
        grid.add(editDonGia, 1, 3);
        grid.add(new Label("Ngày tạo"), 0, 4);
        grid.add(editNgay, 1, 4);
        grid.add(new Label("Ghi chú"), 0, 5);
        grid.add(editGhiChu, 1, 5);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ok) {
            try {
                item.setMaKhoanThu(editMa.getText().trim());
                item.setTenKhoanThu(editTen.getText().trim());
                item.setLoaiPhi(editLoai.getValue());
                item.setDonGia(new BigDecimal(editDonGia.getText().trim()));
                item.setNgayTao(editNgay.getValue());
                item.setGhiChu(editGhiChu.getText().trim());
                boolean updated = feeService.updateFee(item);
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật khoản thu thành công.");
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật khoản thu.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Dữ liệu nhập không hợp lệ.");
            }
        }
    }

    private void handleDelete(KhoanThu item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa không?");
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean deleted = feeService.deleteFee(item.getId());
            if (deleted) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa khoản thu thành công.");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa khoản thu.");
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
