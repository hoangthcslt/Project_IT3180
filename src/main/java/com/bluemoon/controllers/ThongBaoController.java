package com.bluemoon.controllers;

import com.bluemoon.models.ThongBao;
import com.bluemoon.models.UserGroup;
import com.bluemoon.repositories.PermissionRepository;
import com.bluemoon.services.ThongBaoService;
import com.bluemoon.utils.ExcelExporter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThongBaoController {
    private static final List<String> STATUSES = List.of("Đã phát hành", "Nháp", "Ẩn");

    @FXML
    private Button btnSearchNotification;
    @FXML
    private Button btnAddNotification;
    @FXML
    private Button btnExportExcel;
    @FXML
    private Button btnResetSearch;
    @FXML
    private TableView<ThongBao> tableThongBao;
    @FXML
    private TableColumn<ThongBao, Number> colStt;
    @FXML
    private TableColumn<ThongBao, String> colTitle;
    @FXML
    private TableColumn<ThongBao, String> colFile;
    @FXML
    private TableColumn<ThongBao, LocalDate> colNgayBanHanh;
    @FXML
    private TableColumn<ThongBao, String> colGroups;
    @FXML
    private TableColumn<ThongBao, String> colStatus;
    @FXML
    private TableColumn<ThongBao, Void> colActions;

    private final ThongBaoService service = new ThongBaoService();
    private final PermissionRepository permissionRepository = new PermissionRepository();
    private final ExcelExporter excelExporter = new ExcelExporter();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        colStt.setCellValueFactory(
                row -> new ReadOnlyObjectWrapper<>(tableThongBao.getItems().indexOf(row.getValue()) + 1));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("tenThongBao"));
        colFile.setCellValueFactory(row -> new ReadOnlyStringWrapper(fileName(row.getValue().getFilePath())));
        colNgayBanHanh.setCellValueFactory(new PropertyValueFactory<>("ngayBanHanh"));
        colGroups.setCellValueFactory(new PropertyValueFactory<>("nhomNhan"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colActions.setCellFactory(col -> actionCell());
        tableThongBao.setItems(FXCollections.observableArrayList());
        tableThongBao.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setupButton(btnSearchNotification, "#0ea5e9", "#0284c7");
        setupButton(btnAddNotification, "#22c55e", "#16a34a");
        setupButton(btnExportExcel, "#6366f1", "#4f46e5");
        setupButton(btnResetSearch, "#64748b", "#475569");
        refreshTable();
    }

    @FXML
    void handleShowAddDialog(ActionEvent event) {
        showEditDialog(null);
    }

    @FXML
    void handleResetSearch(ActionEvent event) {
        refreshTable();
    }

    @FXML
    void handleShowSearchDialog(ActionEvent event) {
        SearchForm form = new SearchForm(groups());
        Dialog<ButtonType> dialog = createDialog("Tìm kiếm thông báo", form.grid);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Integer groupId = form.group.getValue() == null ? null : form.group.getValue().getId();
            String status = "Tất cả".equals(form.status.getValue()) ? null : form.status.getValue();
            tableThongBao.getItems()
                    .setAll(service.searchNotifications(form.title.getText(), form.date.getValue(), status, groupId));
        }
    }

    @FXML
    void handleExportExcel(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu danh sách thông báo");
        chooser.setInitialFileName("DanhSachThongBao_BlueMoon.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = chooser.showSaveDialog(tableThongBao.getScene().getWindow());
        if (file == null)
            return;
        if (excelExporter.exportNotificationsToExcel(service.getAllNotifications(), file.getAbsolutePath())) {
            showAlert(Alert.AlertType.INFORMATION, "Xuất danh sách thông báo thành công.");
        } else
            showAlert(Alert.AlertType.ERROR, "Không thể xuất danh sách thông báo.");
    }

    private void showEditDialog(ThongBao existing) {
        boolean adding = existing == null;
        NotificationForm form = new NotificationForm(groups());
        if (!adding)
            form.populate(existing, service.getGroupIdsByNotification(existing.getId()));
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(adding ? "Thêm thông báo" : "Chỉnh sửa thông báo");
        dialog.initOwner(tableThongBao.getScene().getWindow());
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(form.grid);
        ((Button) dialog.getDialogPane().lookupButton(ok)).addEventFilter(ActionEvent.ACTION, event -> {
            try {
                ThongBao item = adding ? new ThongBao() : existing;
                item.setTenThongBao(form.title.getText().trim());
                item.setFilePath(form.filePath);
                item.setNgayBanHanh(form.date.getValue());
                item.setTrangThai(form.status.getValue());
                List<Integer> groupIds = form.selectedGroupIds();
                validate(item, groupIds);
                boolean saved = adding ? service.addNotification(item, groupIds)
                        : service.updateNotification(item, groupIds);
                if (!saved)
                    throw new IllegalArgumentException("Không thể lưu thông báo.");
                refreshTable();
                refreshDashboard();
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.WARNING, e.getMessage());
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    private void validate(ThongBao item, List<Integer> groups) {
        if (item.getTenThongBao().isBlank())
            throw new IllegalArgumentException("Tên thông báo không được để trống.");
        if (item.getNgayBanHanh() == null)
            throw new IllegalArgumentException("Vui lòng chọn ngày ban hành.");
        if (item.getTrangThai() == null)
            throw new IllegalArgumentException("Vui lòng chọn trạng thái.");
        if (groups.isEmpty())
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một nhóm nhận.");
    }

    private List<UserGroup> groups() {
        return permissionRepository.findGroups("").stream()
                .filter(group -> !"Trống".equalsIgnoreCase(group.getTenNhom())).toList();
    }

    private void refreshTable() {
        tableThongBao.getItems().setAll(service.getAllNotifications());
    }

    private void refreshDashboard() {
        if (dashboardController != null)
            dashboardController.refreshNotifications();
    }

    private TableCell<ThongBao, Void> actionCell() {
        return new TableCell<>() {
            private final Button edit = actionButton("Sửa");
            private final Button delete = actionButton("Xóa");
            private final HBox box = new HBox(6, edit, delete);
            {
                box.setAlignment(Pos.CENTER);
                edit.setOnAction(event -> showEditDialog(getTableView().getItems().get(getIndex())));
                delete.setOnAction(event -> delete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    private void delete(ThongBao item) {
        ButtonType delete = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa thông báo này?", ButtonType.CANCEL,
                delete);
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == delete && service.deleteNotification(item.getId())) {
            refreshTable();
            refreshDashboard();
        }
    }

    private Dialog<ButtonType> createDialog(String title, GridPane content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(tableThongBao.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);
        return dialog;
    }

    private Button actionButton(String text) {
        Button button = new Button(text);
        button.setMinSize(44, 28);
        button.setStyle("-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-cursor: hand;");
        return button;
    }

    private void setupButton(Button button, String color, String hover) {
        String base = "-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 7; -fx-cursor: hand;";
        String active = base.replace(color, hover);
        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(active));
        button.setOnMouseExited(e -> button.setStyle(base));
    }

    private String fileName(String path) {
        return path == null || path.isBlank() ? "Chưa có file" : new File(path).getName();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private class NotificationForm {
        final GridPane grid = grid();
        final TextField title = new TextField();
        final Label fileLabel = new Label("Chưa chọn file");
        final DatePicker date = new DatePicker();
        final ComboBox<String> status = new ComboBox<>();
        final MenuButton groupMenu = new MenuButton("Chọn nhóm nhận");
        String filePath;

        NotificationForm(List<UserGroup> groups) {
            title.setPromptText("Nhập tiêu đề thông báo");
            Button choose = new Button("Chọn file");
            choose.setOnAction(e -> chooseFile());
            status.getItems().addAll(STATUSES);
            for (UserGroup group : groups) {
                CheckBox box = new CheckBox(group.getTenNhom());
                box.setUserData(group.getId());
                box.setOnAction(e -> updateMenuText());
                CustomMenuItem item = new CustomMenuItem(box);
                item.setHideOnClick(false);
                groupMenu.getItems().add(item);
            }
            add(grid, "Tên thông báo", title, 0);
            add(grid, "File đính kèm", new HBox(8, choose, fileLabel), 1);
            add(grid, "Ngày ban hành", date, 2);
            add(grid, "Nhóm nhận", groupMenu, 3);
            add(grid, "Trạng thái", status, 4);
        }

        void chooseFile() {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tệp hỗ trợ", "*.pdf", "*.doc", "*.docx",
                    "*.xlsx", "*.jpg", "*.png"));
            File file = chooser.showOpenDialog(tableThongBao.getScene().getWindow());
            if (file != null) {
                filePath = file.getAbsolutePath();
                fileLabel.setText(file.getName());
            }
        }

        void populate(ThongBao item, List<Integer> ids) {
            title.setText(item.getTenThongBao());
            filePath = item.getFilePath();
            fileLabel.setText(fileName(filePath));
            date.setValue(item.getNgayBanHanh());
            status.setValue(item.getTrangThai());
            for (MenuItem itemMenu : groupMenu.getItems()) {
                if (itemMenu instanceof CustomMenuItem cmi && cmi.getContent() instanceof CheckBox box) {
                    box.setSelected(ids.contains((Integer) box.getUserData()));
                }
            }
            updateMenuText();
        }

        List<Integer> selectedGroupIds() {
            List<Integer> ids = new ArrayList<>();
            for (MenuItem itemMenu : groupMenu.getItems()) {
                if (itemMenu instanceof CustomMenuItem cmi && cmi.getContent() instanceof CheckBox box
                        && box.isSelected()) {
                    ids.add((Integer) box.getUserData());
                }
            }
            return ids;
        }

        void updateMenuText() {
            List<String> names = new ArrayList<>();
            for (MenuItem itemMenu : groupMenu.getItems()) {
                if (itemMenu instanceof CustomMenuItem cmi && cmi.getContent() instanceof CheckBox box
                        && box.isSelected()) {
                    names.add(box.getText());
                }
            }
            if (names.isEmpty())
                groupMenu.setText("Chọn nhóm nhận");
            else if (names.size() > 2)
                groupMenu.setText(names.size() + " nhóm được chọn");
            else
                groupMenu.setText(String.join(", ", names));
        }
    }

    private static class SearchForm {
        final GridPane grid = grid();
        final TextField title = new TextField();
        final DatePicker date = new DatePicker();
        final ComboBox<UserGroup> group = new ComboBox<>();
        final ComboBox<String> status = new ComboBox<>();

        SearchForm(List<UserGroup> groups) {
            title.setPromptText("Nhập tiêu đề thông báo");
            group.getItems().addAll(groups);
            group.setConverter(new StringConverter<>() {
                @Override
                public String toString(UserGroup value) {
                    return value == null ? "" : value.getTenNhom();
                }

                @Override
                public UserGroup fromString(String value) {
                    return null;
                }
            });
            group.setPromptText("Tất cả");
            status.getItems().addAll("Tất cả", "Đã phát hành", "Nháp", "Ẩn");
            status.setValue("Tất cả");
            add(grid, "Tên thông báo", title, 0);
            add(grid, "Ngày ban hành", date, 1);
            add(grid, "Nhóm nhận", group, 2);
            add(grid, "Trạng thái", status, 3);
        }
    }

    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(18));
        return grid;
    }

    private static void add(GridPane grid, String label, Node field, int row) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
    }
}
