package com.bluemoon.controllers;

import com.bluemoon.models.ThongBao;
import com.bluemoon.services.ThongBaoService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Pos;

public class ThongBaoController {

    @FXML
    private TextField txtTitle;
    @FXML
    private Button btnChooseFile;
    @FXML
    private Label lblSelectedFile;
    @FXML
    private DatePicker dpNgayBanHanh;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private Button btnResetForm;
    @FXML
    private Button btnAddNotification;

    @FXML
    private TextField txtSearchTitle;
    @FXML
    private DatePicker dpSearchDate;
    @FXML
    private ComboBox<String> cbSearchStatus;
    @FXML
    private Button btnResetSearch;
    @FXML
    private Button btnSearchNotification;

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
    private TableColumn<ThongBao, String> colStatus;
    @FXML
    private TableColumn<ThongBao, Void> colActions;

    private final ThongBaoService thongBaoService = new ThongBaoService();
    private String selectedFilePath;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("Đã xuất bản", "Chưa xuất bản"));
        cbSearchStatus.setItems(FXCollections.observableArrayList("Đã xuất bản", "Chưa xuất bản"));

        colStt.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(tableThongBao.getItems().indexOf(cellData.getValue()) + 1));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("tenThongBao"));
        colFile.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(formatFileDisplay(cellData.getValue().getFilePath())));
        colNgayBanHanh.setCellValueFactory(new PropertyValueFactory<>("ngayBanHanh"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colActions.setCellFactory(col -> actionCell());

        tableThongBao.setItems(FXCollections.observableArrayList());
        refreshTable();
        resetFormFields();
    }

    @FXML
    void handleChooseFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file thông báo");
        File file = chooser.showOpenDialog(getWindow());
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            lblSelectedFile.setText(file.getName());
        }
    }

    @FXML
    void handleAddNotification(ActionEvent event) {
        try {
            String title = txtTitle.getText().trim();
            LocalDate ngayBanHanh = dpNgayBanHanh.getValue();
            String trangThai = cbStatus.getValue();

            if (title.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng nhập tên thông báo.");
                return;
            }
            if (ngayBanHanh == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ngày ban hành.");
                return;
            }
            if (trangThai == null || trangThai.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn trạng thái.");
                return;
            }

            ThongBao thongBao = new ThongBao();
            thongBao.setTenThongBao(title);
            thongBao.setFilePath(selectedFilePath);
            thongBao.setNgayBanHanh(ngayBanHanh);
            thongBao.setTrangThai(trangThai);

            boolean saved = thongBaoService.addNotification(thongBao);
            if (saved) {
                showAlert(Alert.AlertType.INFORMATION, "Thêm thông báo thành công.");
                resetFormFields();
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Không thể thêm thông báo. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Đã có lỗi xảy ra: " + e.getMessage());
        }
    }

    @FXML
    void handleResetForm(ActionEvent event) {
        resetFormFields();
    }

    @FXML
    void handleSearchNotification(ActionEvent event) {
        String title = txtSearchTitle.getText().trim();
        LocalDate ngayBanHanh = dpSearchDate.getValue();
        String trangThai = cbSearchStatus.getValue();
        List<ThongBao> result = thongBaoService.searchNotifications(title, ngayBanHanh, trangThai);
        tableThongBao.getItems().setAll(result);
    }

    @FXML
    void handleResetSearch(ActionEvent event) {
        txtSearchTitle.clear();
        dpSearchDate.setValue(null);
        cbSearchStatus.setValue(null);
        refreshTable();
    }

    private void refreshTable() {
        tableThongBao.getItems().setAll(thongBaoService.getAllNotifications());
    }

    private void resetFormFields() {
        txtTitle.clear();
        selectedFilePath = null;
        lblSelectedFile.setText("Chưa chọn file");
        dpNgayBanHanh.setValue(null);
        cbStatus.setValue(null);
    }

    private void showEditDialog(ThongBao item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông báo");
        dialog.setHeaderText(null);

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField editTitle = new TextField(item.getTenThongBao());
        editTitle.setPrefWidth(360);
        Button chooseEditFile = new Button("Chọn file");
        chooseEditFile.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 14;");
        Label lblEditFile = new Label(formatFileDisplay(item.getFilePath()));
        DatePicker editDate = new DatePicker(item.getNgayBanHanh());
        ComboBox<String> editStatus = new ComboBox<>(FXCollections.observableArrayList("Đã xuất bản", "Chưa xuất bản"));
        editStatus.setValue(item.getTrangThai());

        final String[] editFilePath = { item.getFilePath() };
        chooseEditFile.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Chọn file thông báo");
            File result = chooser.showOpenDialog(getWindow());
            if (result != null) {
                editFilePath[0] = result.getAbsolutePath();
                lblEditFile.setText(result.getName());
            }
        });

        grid.add(new Label("Tên thông báo"), 0, 0);
        grid.add(editTitle, 1, 0, 2, 1);
        grid.add(new Label("File thông báo"), 0, 1);
        HBox fileBox = new HBox(10, chooseEditFile, lblEditFile);
        grid.add(fileBox, 1, 1, 2, 1);
        grid.add(new Label("Ngày ban hành"), 0, 2);
        grid.add(editDate, 1, 2);
        grid.add(new Label("Trạng thái"), 0, 3);
        grid.add(editStatus, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButton) {
            if (editTitle.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Tên thông báo không được để trống.");
                return;
            }
            if (editDate.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ngày ban hành.");
                return;
            }
            if (editStatus.getValue() == null || editStatus.getValue().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn trạng thái.");
                return;
            }

            item.setTenThongBao(editTitle.getText().trim());
            item.setFilePath(editFilePath[0]);
            item.setNgayBanHanh(editDate.getValue());
            item.setTrangThai(editStatus.getValue());

            boolean updated = thongBaoService.updateNotification(item);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Cập nhật thông báo thành công.");
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Không thể cập nhật thông báo. Vui lòng thử lại.");
            }
        }
    }

    private void handleDeleteNotification(ThongBao item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa không?");

        ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            boolean deleted = thongBaoService.deleteNotification(item.getId());
            if (deleted) {
                showAlert(Alert.AlertType.INFORMATION, "Xóa thông báo thành công.");
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Không thể xóa thông báo. Vui lòng thử lại.");
            }
        }
    }

    private String formatFileDisplay(String path) {
        if (path == null || path.isBlank()) {
            return "Chưa có file";
        }
        File file = new File(path);
        return file.getName().isBlank() ? path : file.getName();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Window getWindow() {
        return btnChooseFile.getScene().getWindow();
    }

    private TableCell<ThongBao, Void> actionCell() {
        return new TableCell<>() {
            private final Button editButton = createActionButton("✎");
            private final Button deleteButton = createActionButton("🗑");
            private final HBox box = new HBox(8, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteNotification(getTableView().getItems().get(getIndex())));
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
}
