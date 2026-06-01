package com.bluemoon.controllers;

import com.bluemoon.models.HoKhau;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.HouseholdService;
import com.bluemoon.services.ResidentService;
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
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ResidentController {
    private static final List<String> GENDERS = List.of("Nam", "Nữ");
    private static final List<String> RELATIONSHIPS = List.of("Chủ hộ", "Vợ", "Chồng", "Con", "Ông", "Bà", "Anh", "Chị", "Em", "Người thuê", "Khác");
    private static final List<String> STATUSES = List.of("Thường trú", "Tạm trú", "Tạm vắng");

    @FXML private Button btnSearchResident;
    @FXML private Button btnAddResident;
    @FXML private Button btnExportExcel;
    @FXML private Button btnResetSearch;
    @FXML private TableView<NhanKhau> tableNhanKhau;
    @FXML private TableColumn<NhanKhau, Integer> colId;
    @FXML private TableColumn<NhanKhau, Integer> colHoKhauId;
    @FXML private TableColumn<NhanKhau, String> colHoTen;
    @FXML private TableColumn<NhanKhau, String> colCccd;
    @FXML private TableColumn<NhanKhau, String> colSoDienThoai;
    @FXML private TableColumn<NhanKhau, LocalDate> colNgaySinh;
    @FXML private TableColumn<NhanKhau, String> colGioiTinh;
    @FXML private TableColumn<NhanKhau, String> colQuanHe;
    @FXML private TableColumn<NhanKhau, String> colTrangThai;
    @FXML private TableColumn<NhanKhau, Void> colHoatDong;

    private final ResidentService residentService = new ResidentService();
    private final HouseholdService householdService = new HouseholdService();
    private final ExcelExporter excelExporter = new ExcelExporter();
    private final ObservableList<NhanKhau> residents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getId()));
        colHoKhauId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getHoKhauId()));
        colHoTen.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getHoTen()));
        colCccd.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getCccd()));
        colSoDienThoai.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getSoDienThoai()));
        colNgaySinh.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getNgaySinh()));
        colGioiTinh.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getGioiTinh()));
        colQuanHe.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getQuanHe()));
        colTrangThai.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTrangThai()));
        colHoatDong.setCellFactory(col -> actionCell());
        tableNhanKhau.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableNhanKhau.setItems(residents);
        setupButton(btnSearchResident, "#0ea5e9", "#0284c7");
        setupButton(btnAddResident, "#22c55e", "#16a34a");
        setupButton(btnExportExcel, "#6366f1", "#4f46e5");
        setupButton(btnResetSearch, "#64748b", "#475569");
        loadData();
    }

    private void loadData() { residents.setAll(residentService.getAllResidents()); }
    @FXML void handleShowAddDialog(ActionEvent event) { showEditDialog(null); }
    @FXML void handleResetSearch(ActionEvent event) { loadData(); }

    @FXML
    void handleShowSearchDialog(ActionEvent event) {
        ResidentForm form = new ResidentForm(false, householdService.getAllHouseholds());
        Dialog<ButtonType> dialog = createDialog("Tìm kiếm nhân khẩu", form.grid);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            residents.setAll(residentService.searchResidents(form.householdId(), form.name.getText(),
                    form.cccd.getText(), form.phone.getText(), form.birthday.getValue(), form.gender.getValue(),
                    form.relationship.getValue(), form.status.getValue()));
        }
    }

    @FXML
    void handleExportExcel(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu danh sách nhân khẩu");
        chooser.setInitialFileName("DanhSachNhanKhau_BlueMoon.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = chooser.showSaveDialog(tableNhanKhau.getScene().getWindow());
        if (file == null) return;
        if (excelExporter.exportResidentsToExcel(residentService.getAllResidents(), file.getAbsolutePath())) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất danh sách nhân khẩu thành công.");
        } else showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất danh sách nhân khẩu.");
    }

    private void showEditDialog(NhanKhau resident) {
        boolean adding = resident == null;
        ResidentForm form = new ResidentForm(true, householdService.getAllHouseholds());
        if (!adding) form.populate(resident);
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(adding ? "Thêm nhân khẩu" : "Chỉnh sửa nhân khẩu");
        dialog.initOwner(tableNhanKhau.getScene().getWindow());
        ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(form.grid);
        ((Button) dialog.getDialogPane().lookupButton(okType)).addEventFilter(ActionEvent.ACTION, event -> {
            try {
                NhanKhau value = form.toResident(adding ? 0 : resident.getId());
                boolean success = adding ? residentService.addResident(value) : residentService.updateResident(value);
                if (!success) throw new IllegalArgumentException("Không thể lưu nhân khẩu.");
                loadData();
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Dữ liệu chưa hợp lệ", e.getMessage());
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    private Dialog<ButtonType> createDialog(String title, GridPane content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(tableNhanKhau.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);
        return dialog;
    }

    private TableCell<NhanKhau, Void> actionCell() {
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

    private void confirmDelete(NhanKhau resident) {
        ButtonType delete = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa nhân khẩu này?", ButtonType.CANCEL, delete);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == delete) {
            if (residentService.deleteResident(resident.getId())) loadData();
            else showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa nhân khẩu.");
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

    private static class ResidentForm {
        private final GridPane grid = new GridPane();
        private final ComboBox<HoKhau> household = new ComboBox<>();
        private final TextField name = new TextField();
        private final TextField cccd = new TextField();
        private final TextField phone = new TextField();
        private final DatePicker birthday = new DatePicker();
        private final ComboBox<String> gender = new ComboBox<>();
        private final ComboBox<String> relationship = new ComboBox<>();
        private final ComboBox<String> status = new ComboBox<>();

        ResidentForm(boolean required, List<HoKhau> households) {
            household.getItems().addAll(households);
            household.setConverter(new StringConverter<>() {
                @Override public String toString(HoKhau value) { return value == null ? "" : value.getMaHoKhau() + " - " + value.getTenChuHo(); }
                @Override public HoKhau fromString(String value) { return null; }
            });
            household.setPromptText("Chọn hộ khẩu");
            name.setPromptText("Nhập đầy đủ họ tên");
            cccd.setPromptText("12 chữ số");
            phone.setPromptText("10 hoặc 11 chữ số");
            cccd.setTextFormatter(digitsFormatter(12));
            phone.setTextFormatter(digitsFormatter(11));
            gender.getItems().addAll(GENDERS);
            relationship.getItems().addAll(RELATIONSHIPS);
            status.getItems().addAll(STATUSES);
            grid.setHgap(14);
            grid.setVgap(10);
            grid.setPadding(new Insets(18));
            add("ID Hộ khẩu", household, 0);
            add("Họ tên", name, 1);
            add("CCCD", cccd, 2);
            add("SĐT", phone, 3);
            add("Ngày sinh", birthday, 4);
            add("Giới tính", gender, 5);
            add("Quan hệ", relationship, 6);
            add("Trạng thái", status, 7);
        }

        Integer householdId() { return household.getValue() == null ? null : household.getValue().getId(); }

        void populate(NhanKhau resident) {
            household.getItems().stream().filter(item -> item.getId() == resident.getHoKhauId()).findFirst().ifPresent(household::setValue);
            name.setText(resident.getHoTen());
            cccd.setText(resident.getCccd());
            phone.setText(resident.getSoDienThoai());
            birthday.setValue(resident.getNgaySinh());
            gender.setValue(resident.getGioiTinh());
            relationship.setValue(resident.getQuanHe());
            status.setValue(resident.getTrangThai());
        }

        NhanKhau toResident(int id) {
            if (householdId() == null) throw new IllegalArgumentException("Vui lòng chọn hộ khẩu.");
            return new NhanKhau(id, householdId(), name.getText().trim(), cccd.getText().trim(), phone.getText().trim(),
                    birthday.getValue(), gender.getValue(), relationship.getValue(), status.getValue());
        }

        private void add(String label, Control field, int row) {
            field.setPrefWidth(260);
            grid.add(new Label(label), 0, row);
            grid.add(field, 1, row);
        }

        private static TextFormatter<String> digitsFormatter(int maxLength) {
            return new TextFormatter<>(change -> change.getControlNewText().matches("\\d{0," + maxLength + "}") ? change : null);
        }
    }
}
