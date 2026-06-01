package com.bluemoon.controllers;

import com.bluemoon.models.HoKhau;
import com.bluemoon.services.HouseholdService;
import com.bluemoon.utils.ExcelExporter;
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
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HouseholdController {
    private static final List<String> STATUSES = List.of("Đang ở", "Cho thuê", "Trống");

    @FXML private Button btnAddHousehold;
    @FXML private Button btnSearchHousehold;
    @FXML private Button btnExportExcel;
    @FXML private Button btnResetSearch;
    @FXML private TableView<HoKhau> tableHoKhau;
    @FXML private TableColumn<HoKhau, Integer> colId;
    @FXML private TableColumn<HoKhau, String> colMaHoKhau;
    @FXML private TableColumn<HoKhau, String> colTenChuHo;
    @FXML private TableColumn<HoKhau, BigDecimal> colDienTich;
    @FXML private TableColumn<HoKhau, String> colTrangThai;
    @FXML private TableColumn<HoKhau, Integer> colSoNguoi;
    @FXML private TableColumn<HoKhau, String> colPhuongTien;
    @FXML private TableColumn<HoKhau, LocalDate> colNgayLap;
    @FXML private TableColumn<HoKhau, Void> colHoatDong;

    private final HouseholdService householdService = new HouseholdService();
    private final ExcelExporter excelExporter = new ExcelExporter();
    private final ObservableList<HoKhau> householdList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getId()));
        colMaHoKhau.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getMaHoKhau()));
        colTenChuHo.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTenChuHo()));
        colDienTich.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getDienTich()));
        colTrangThai.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTrangThai()));
        colSoNguoi.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getSoNguoi()));
        colPhuongTien.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getPhuongTien()));
        colNgayLap.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getNgayLap()));
        colHoatDong.setCellFactory(col -> actionCell());
        tableHoKhau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableHoKhau.setItems(householdList);

        setupButton(btnSearchHousehold, "#0ea5e9", "#0284c7");
        setupButton(btnAddHousehold, "#22c55e", "#16a34a");
        setupButton(btnExportExcel, "#6366f1", "#4f46e5");
        setupButton(btnResetSearch, "#64748b", "#475569");
        loadData();
    }

    private void loadData() { householdList.setAll(householdService.getAllHouseholds()); }

    @FXML void handleShowAddDialog(ActionEvent event) { showEditDialog(null); }
    @FXML void handleShowSearchDialog(ActionEvent event) { showSearchDialog(); }
    @FXML void handleResetSearch(ActionEvent event) { loadData(); }

    @FXML
    void handleExportExcel(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu danh sách hộ khẩu");
        chooser.setInitialFileName("DanhSachHoKhau_BlueMoon.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = chooser.showSaveDialog(tableHoKhau.getScene().getWindow());
        if (file == null) return;
        if (excelExporter.exportHouseholdsToExcel(householdService.getAllHouseholds(), file.getAbsolutePath())) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất danh sách hộ khẩu thành công.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất danh sách hộ khẩu.");
        }
    }

    private void showSearchDialog() {
        HouseholdForm form = new HouseholdForm(false);
        Dialog<ButtonType> dialog = createDialog("Tìm kiếm hộ khẩu", form.grid);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                validatePositiveSearchNumbers(form);
                householdList.setAll(householdService.searchHouseholds(form.ma.getText(), form.ten.getText(),
                        form.dienTich.getText(), form.trangThai.getValue(), form.soNguoi.getText(),
                        form.phuongTien.getText()));
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Dữ liệu chưa hợp lệ", e.getMessage());
            }
        }
    }

    private void validatePositiveSearchNumbers(HouseholdForm form) {
        if (!form.dienTich.getText().isBlank() && new BigDecimal(form.dienTich.getText()).signum() <= 0) {
            throw new IllegalArgumentException("Diện tích phải lớn hơn 0.");
        }
        if (!form.soNguoi.getText().isBlank() && Integer.parseInt(form.soNguoi.getText()) < 0) {
            throw new IllegalArgumentException("Số người không được nhỏ hơn 0.");
        }
    }

    private void showEditDialog(HoKhau household) {
        boolean adding = household == null;
        HouseholdForm form = new HouseholdForm(true);
        if (!adding) form.populate(household);
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(adding ? "Thêm hộ khẩu" : "Chỉnh sửa hộ khẩu");
        dialog.initOwner(tableHoKhau.getScene().getWindow());
        ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(form.grid);
        ((Button) dialog.getDialogPane().lookupButton(okType)).addEventFilter(ActionEvent.ACTION, event -> {
            if (!saveHousehold(form, household)) event.consume();
        });
        dialog.showAndWait();
    }

    private boolean saveHousehold(HouseholdForm form, HoKhau existing) {
        try {
            HoKhau household = form.toHousehold(existing == null ? 0 : existing.getId());
            boolean duplicate = existing == null
                    ? householdService.isMaHoKhauExists(household.getMaHoKhau())
                    : householdService.isMaHoKhauExists(household.getMaHoKhau(), existing.getId());
            if (duplicate) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mã hộ khẩu đã tồn tại.");
                return false;
            }
            boolean success = existing == null ? householdService.addHousehold(household)
                    : householdService.updateHousehold(household);
            if (!success) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu hộ khẩu.");
                return false;
            }
            loadData();
            return true;
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Dữ liệu chưa hợp lệ", e.getMessage());
            return false;
        }
    }

    private Dialog<ButtonType> createDialog(String title, GridPane content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(tableHoKhau.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);
        return dialog;
    }

    private TableCell<HoKhau, Void> actionCell() {
        return new TableCell<>() {
            private final Button edit = createActionButton("Sửa");
            private final Button delete = createActionButton("Xóa");
            private final HBox box = new HBox(6, edit, delete);
            {
                box.setAlignment(Pos.CENTER);
                edit.setOnAction(event -> showEditDialog(getTableView().getItems().get(getIndex())));
                delete.setOnAction(event -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    private void confirmDelete(HoKhau household) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa hộ khẩu này?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (householdService.deleteHousehold(household.getId())) loadData();
            else showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa hộ khẩu.");
        }
    }

    private Button createActionButton(String text) {
        Button button = new Button(text);
        button.setMinSize(44, 28);
        button.setStyle("-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-cursor: hand;");
        return button;
    }

    private void setupButton(Button button, String color, String hover) {
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 7; -fx-cursor: hand;";
        String active = base.replace(color, hover);
        button.setStyle(base);
        button.setOnMouseEntered(event -> button.setStyle(active));
        button.setOnMouseExited(event -> button.setStyle(base));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static class HouseholdForm {
        private final GridPane grid = new GridPane();
        private final TextField ma = new TextField();
        private final TextField ten = new TextField();
        private final TextField dienTich = new TextField();
        private final ComboBox<String> trangThai = new ComboBox<>();
        private final TextField soNguoi = new TextField();
        private final TextField phuongTien = new TextField();
        private final DatePicker ngayLap = new DatePicker();

        HouseholdForm(boolean includeDate) {
            ma.setPromptText("Nhập P + số phòng");
            ten.setPromptText("Nhập rõ họ tên");
            phuongTien.setPromptText("Xe máy, ô tô hoặc số lượng");
            dienTich.setTextFormatter(decimalFormatter());
            soNguoi.setTextFormatter(integerFormatter());
            trangThai.getItems().addAll(STATUSES);
            grid.setHgap(14);
            grid.setVgap(12);
            grid.setPadding(new Insets(18));
            add("Mã hộ khẩu", ma, 0);
            add("Tên chủ hộ", ten, 1);
            add("Diện tích (m²)", dienTich, 2);
            add("Trạng thái", trangThai, 3);
            add("Số người", soNguoi, 4);
            add("Phương tiện", phuongTien, 5);
            if (includeDate) add("Ngày lập", ngayLap, 6);
        }

        void populate(HoKhau household) {
            ma.setText(household.getMaHoKhau());
            ten.setText(household.getTenChuHo());
            dienTich.setText(household.getDienTich().toPlainString());
            trangThai.setValue(household.getTrangThai());
            soNguoi.setText(String.valueOf(household.getSoNguoi()));
            phuongTien.setText(household.getPhuongTien());
            ngayLap.setValue(household.getNgayLap());
        }

        HoKhau toHousehold(int id) {
            if (ma.getText().isBlank() || ten.getText().isBlank() || dienTich.getText().isBlank()
                    || trangThai.getValue() == null || soNguoi.getText().isBlank()
                    || phuongTien.getText().isBlank() || ngayLap.getValue() == null) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin.");
            }
            return new HoKhau(id, ma.getText().trim(), ten.getText().trim(), new BigDecimal(dienTich.getText()),
                    trangThai.getValue(), Integer.parseInt(soNguoi.getText()), phuongTien.getText().trim(),
                    ngayLap.getValue());
        }

        private void add(String label, Control field, int row) {
            field.setPrefWidth(240);
            grid.add(new Label(label), 0, row);
            grid.add(field, 1, row);
        }

        private static TextFormatter<String> decimalFormatter() {
            return new TextFormatter<>(change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null);
        }

        private static TextFormatter<String> integerFormatter() {
            return new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null);
        }
    }
}
