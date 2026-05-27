package com.bluemoon.controllers;

import com.bluemoon.models.HoKhau;
import com.bluemoon.services.HouseholdService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HouseholdController {

    @FXML
    private TextField txtMaHoKhau;
    @FXML
    private TextField txtTenChuHo;
    @FXML
    private TextField txtDienTich;
    @FXML
    private DatePicker dpNgayLap;
    @FXML
    private TextField txtSearchMaHoKhau;
    @FXML
    private TextField txtSearchTenChuHo;
    @FXML
    private TextField txtSearchDienTich;
    @FXML
    private DatePicker dpSearchNgayLap;
    @FXML
    private Button btnAddHousehold;
    @FXML
    private Button btnResetAdd;
    @FXML
    private Button btnSearchHousehold;
    @FXML
    private Button btnResetSearch;

    @FXML
    private TableView<HoKhau> tableHoKhau;
    @FXML
    private TableColumn<HoKhau, Integer> colId;
    @FXML
    private TableColumn<HoKhau, String> colMaHoKhau;
    @FXML
    private TableColumn<HoKhau, String> colTenChuHo;
    @FXML
    private TableColumn<HoKhau, BigDecimal> colDienTich;
    @FXML
    private TableColumn<HoKhau, LocalDate> colNgayLap;
    @FXML
    private TableColumn<HoKhau, Void> colHoatDong;

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
        colHoatDong.setCellFactory(col -> actionCell());
        tableHoKhau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        loadData();

        // Validation for Diện tích: only allow numbers and decimal points
        txtDienTich.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));
        txtSearchDienTich.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));
        setupDashboardButton(btnAddHousehold, "#27ae60", "#219653");
        setupDashboardButton(btnSearchHousehold, "#27ae60", "#219653");
        setupDashboardButton(btnResetAdd, "#7f8c8d", "#6f7c7d");
        setupDashboardButton(btnResetSearch, "#7f8c8d", "#6f7c7d");
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

            if (ma == null || ma.isEmpty() || ten == null || ten.isEmpty() || dienTichStr == null
                    || dienTichStr.isEmpty() || ngayLap == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            if (householdService.isMaHoKhauExists(ma)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mã hộ khẩu này đã tồn tại trong hệ thống!");
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
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm hộ khẩu.");
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

    @FXML
    void handleSearch(ActionEvent event) {
        try {
            householdList.setAll(householdService.searchHouseholds(
                    txtSearchMaHoKhau.getText(),
                    txtSearchTenChuHo.getText(),
                    txtSearchDienTich.getText(),
                    dpSearchNgayLap.getValue()));
            tableHoKhau.setItems(householdList);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Diện tích phải là số hợp lệ.");
        }
    }

    @FXML
    void handleResetSearch(ActionEvent event) {
        txtSearchMaHoKhau.clear();
        txtSearchTenChuHo.clear();
        txtSearchDienTich.clear();
        dpSearchNgayLap.setValue(null);
        loadData();
    }

    private TableCell<HoKhau, Void> actionCell() {
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

    private Button createActionButton(String text) {
        Button button = new Button(text);
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
        String baseStyle = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;";
        button.setStyle(baseStyle);
        button.setOnMouseEntered(event -> button.setStyle(hoverStyle));
        button.setOnMouseExited(event -> button.setStyle(baseStyle));
    }

    private void showEditDialog(HoKhau hoKhau) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa hộ khẩu");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtEditMa = new TextField(hoKhau.getMaHoKhau());
        TextField txtEditTen = new TextField(hoKhau.getTenChuHo());
        TextField txtEditDienTich = new TextField(hoKhau.getDienTich().toPlainString());
        DatePicker dpEditNgayLap = new DatePicker(hoKhau.getNgayLap());
        txtEditDienTich.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(18));
        form.add(new Label("Mã hộ khẩu"), 0, 0);
        form.add(txtEditMa, 1, 0);
        form.add(new Label("Tên chủ hộ"), 0, 1);
        form.add(txtEditTen, 1, 1);
        form.add(new Label("Diện tích"), 0, 2);
        form.add(txtEditDienTich, 1, 2);
        form.add(new Label("Ngày lập"), 0, 3);
        form.add(dpEditNgayLap, 1, 3);
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (txtEditMa.getText().isBlank() || txtEditTen.getText().isBlank()
                        || txtEditDienTich.getText().isBlank() || dpEditNgayLap.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                    return;
                }
                HoKhau updated = new HoKhau(
                        hoKhau.getId(),
                        txtEditMa.getText(),
                        txtEditTen.getText(),
                        new BigDecimal(txtEditDienTich.getText()),
                        dpEditNgayLap.getValue());
                if (householdService.updateHousehold(updated)) {
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật hộ khẩu.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Diện tích phải là số hợp lệ.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi hệ thống.");
            }
        }
    }

    private void confirmDelete(HoKhau hoKhau) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa không?");
        ButtonType yes = new ButtonType("Có");
        ButtonType no = new ButtonType("Không");
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            if (householdService.deleteHousehold(hoKhau.getId())) {
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa hộ khẩu.");
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
