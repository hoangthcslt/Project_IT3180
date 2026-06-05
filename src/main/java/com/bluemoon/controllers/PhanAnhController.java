package com.bluemoon.controllers;

import com.bluemoon.models.PhanAnh;
import com.bluemoon.services.PhanAnhService;
import com.bluemoon.utils.SessionManager;
import com.bluemoon.models.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhanAnhController {

    @FXML
    private Button btnAddReport;
    @FXML
    private TextField txtSearchCitizen;
    @FXML
    private ComboBox<String> cbFilterStatus;
    @FXML
    private Button btnSearchCitizen;
    @FXML
    private Button btnResetSearch;

    @FXML
    private TableView<PhanAnh> tableCitizenReports;
    @FXML
    private TableColumn<PhanAnh, Number> colCitizenStt;
    @FXML
    private TableColumn<PhanAnh, String> colCitizenTitle;
    @FXML
    private TableColumn<PhanAnh, String> colCitizenCategory;
    @FXML
    private TableColumn<PhanAnh, LocalDate> colCitizenDate;
    @FXML
    private TableColumn<PhanAnh, String> colCitizenStatus;
    @FXML
    private TableColumn<PhanAnh, String> colCitizenAssignee;
    @FXML
    private TableColumn<PhanAnh, Void> colCitizenActions;

    @FXML
    private TableView<PhanAnh> tableAdminReports;
    @FXML
    private TableColumn<PhanAnh, Number> colAdminStt;
    @FXML
    private TableColumn<PhanAnh, String> colAdminTitle;
    @FXML
    private TableColumn<PhanAnh, String> colAdminSender;
    @FXML
    private TableColumn<PhanAnh, LocalDate> colAdminDate;
    @FXML
    private TableColumn<PhanAnh, String> colAdminStatus;
    @FXML
    private TableColumn<PhanAnh, String> colAdminAssignee;
    @FXML
    private TableColumn<PhanAnh, Void> colAdminActions;

    @FXML
    private javafx.scene.control.TabPane tabPane;
    @FXML
    private javafx.scene.control.Tab tabCitizen;
    @FXML
    private javafx.scene.control.Tab tabAdmin;

    private final PhanAnhService phanAnhService = new PhanAnhService();

    @FXML
    public void initialize() {
        cbFilterStatus.setItems(FXCollections.observableArrayList("Tất cả", "Chờ đợi", "Đã tiếp nhận"));
        cbFilterStatus.setValue("Tất cả");

        colCitizenStt.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableCitizenReports.getItems().indexOf(cellData.getValue()) + 1));
        colCitizenTitle.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colCitizenCategory.setCellValueFactory(new PropertyValueFactory<>("linhVuc"));
        colCitizenDate.setCellValueFactory(new PropertyValueFactory<>("ngayGui"));
        colCitizenStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colCitizenAssignee.setCellValueFactory(new PropertyValueFactory<>("nguoiPhuTrach"));
        colCitizenActions.setCellFactory(col -> createCitizenActionCell());

        colAdminStt.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(tableAdminReports.getItems().indexOf(cellData.getValue()) + 1));
        colAdminTitle.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colAdminSender.setCellValueFactory(new PropertyValueFactory<>("nguoiGui"));
        colAdminDate.setCellValueFactory(new PropertyValueFactory<>("ngayGui"));
        colAdminStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colAdminAssignee.setCellValueFactory(new PropertyValueFactory<>("nguoiPhuTrach"));
        colAdminActions.setCellFactory(col -> createAdminActionCell());

        tableCitizenReports.setItems(FXCollections.observableArrayList());
        tableAdminReports.setItems(FXCollections.observableArrayList());

        tableCitizenReports.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableAdminReports.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean hasCitizen = false;
        boolean hasAdmin = false;

        if (currentUser != null) {
            if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                hasCitizen = true;
                hasAdmin = true;
            } else {
                com.bluemoon.services.PermissionService permService = new com.bluemoon.services.PermissionService();
                java.util.Set<String> perms = permService.getPermissionCodesByUser(currentUser.getId());
                if (perms.contains("PHAN_ANH_GUI")) hasCitizen = true;
                if (perms.contains("PHAN_ANH_TIEP_NHAN")) hasAdmin = true;
            }
        }

        if (!hasCitizen) {
            tabPane.getTabs().remove(tabCitizen);
        }
        if (!hasAdmin) {
            tabPane.getTabs().remove(tabAdmin);
        }

        refreshTables();
    }

    @FXML
    void handleAddReport(ActionEvent event) {
        showAddReportDialog();
    }

    @FXML
    void handleSearchCitizen(ActionEvent event) {
        performSearch();
    }

    @FXML
    void handleResetSearch(ActionEvent event) {
        txtSearchCitizen.clear();
        cbFilterStatus.setValue("Tất cả");
        refreshTables();
    }

    private void refreshTables() {
        List<PhanAnh> reports = phanAnhService.getAllReports();
        tableCitizenReports.getItems().setAll(reports);
        tableAdminReports.getItems().setAll(reports);
    }

    private void performSearch() {
        String keyword = txtSearchCitizen.getText();
        String status = cbFilterStatus.getValue();
        List<PhanAnh> result = phanAnhService.searchReports(keyword, status);
        tableCitizenReports.getItems().setAll(result);
    }

    private TableCell<PhanAnh, Void> createCitizenActionCell() {
        return new TableCell<>() {
            private final Button detailButton = createActionButton("Chi ti\u1ebft", "#475569");
            private final Button editButton = createActionButton("S\u1eeda", "#2563eb");
            private final Button deleteButton = createActionButton("X\u00f3a", "#ef4444");

            {
                detailButton.setOnAction(event -> showReportDetailDialog(getCurrentReport(), false));
                editButton.setOnAction(event -> showEditCitizenDialog(getCurrentReport()));
                deleteButton.setOnAction(event -> confirmDelete(getCurrentReport()));
            }

            private PhanAnh getCurrentReport() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, detailButton, editButton, deleteButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        };
    }

    private TableCell<PhanAnh, Void> createAdminActionCell() {
        return new TableCell<>() {
            private final Button detailButton = createActionButton("Chi ti\u1ebft", "#475569");
            private final Button editButton = createActionButton("Edit", "#10b981");

            {
                detailButton.setOnAction(event -> showReportDetailDialog(getCurrentReport(), true));
                editButton.setOnAction(event -> showAdminAssignDialog(getCurrentReport()));
            }

            private PhanAnh getCurrentReport() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, detailButton, editButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        };
    }

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setMinWidth(74);
        button.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14;");
        return button;
    }

    private void showReportDetailDialog(PhanAnh report, boolean adminView) {
        if (report == null) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chi tiet phan anh");
        dialog.setResizable(true);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setPrefWidth(620);

        content.getChildren().add(readOnlyRow("Hinh anh dinh kem", imageSummary(report.getHinhAnh())));
        content.getChildren().add(readOnlyRow("Linh vuc phan anh", report.getLinhVuc()));
        content.getChildren().add(readOnlyRow("Tieu de phan anh", report.getTieuDe()));
        content.getChildren().add(readOnlyArea("Noi dung phan anh", report.getNoiDung()));
        content.getChildren().add(readOnlyRow("Ngay gui", valueOf(report.getNgayGui())));
        if (adminView) {
            content.getChildren().add(readOnlyRow("Nguoi gui", report.getNguoiGui()));
        }
        content.getChildren().add(readOnlyRow("Trang thai xu ly", report.getTrangThai()));
        content.getChildren().add(readOnlyRow("Nguoi tiep nhan", report.getNguoiPhuTrach()));
        content.getChildren().add(readOnlyArea("Phan hoi tu Ban quan ly", report.getPhanHoi()));
        content.getChildren().add(imagePreviewSection(report.getHinhAnh()));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(560);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private Node readOnlyRow(String label, String value) {
        TextField field = new TextField(valueOrEmpty(value));
        field.setEditable(false);
        field.setFocusTraversable(false);
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(5, new Label(label), field);
    }

    private Node readOnlyArea(String label, String value) {
        TextArea area = new TextArea(valueOrEmpty(value));
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(4);
        area.setFocusTraversable(false);
        return new VBox(5, new Label(label), area);
    }

    private Node imagePreviewSection(List<String> imagePaths) {
        HBox images = new HBox(8);
        images.setAlignment(Pos.CENTER_LEFT);
        if (imagePaths == null || imagePaths.isEmpty()) {
            images.getChildren().add(new Label("Khong co hinh anh dinh kem."));
        } else {
            for (String path : imagePaths) {
                File file = new File(path);
                if (file.exists()) {
                    ImageView imageView = new ImageView(new Image(file.toURI().toString(), 120, 90, true, true));
                    imageView.setFitWidth(120);
                    imageView.setFitHeight(90);
                    imageView.setPreserveRatio(true);
                    images.getChildren().add(imageView);
                } else {
                    images.getChildren().add(new Label(file.getName() + " (khong tim thay)"));
                }
            }
        }
        return new VBox(5, new Label("Xem truoc hinh anh"), images);
    }

    private String imageSummary(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return "Khong co";
        }
        return imagePaths.size() + " tep";
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private String valueOrEmpty(String value) {
        return value == null || value.isBlank() ? "" : value;
    }

    private void showAddReportDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm phản ánh");
        dialog.setResizable(true);

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createDialogGrid();

        Label lblImages = new Label("Hình ảnh:");
        Button btnUpload = new Button("Chọn ảnh");
        Label lblImageInfo = new Label("Tối đa 5 ảnh.");
        btnUpload.setStyle(
                "-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12;");
        List<File> selectedImages = new ArrayList<>();
        btnUpload.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Chọn ảnh phản ánh");
            chooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            List<File> files = chooser.showOpenMultipleDialog(dialog.getOwner());
            if (files != null) {
                if (files.size() > 5) {
                    showAlert(Alert.AlertType.WARNING, "Vui lòng chọn tối đa 5 ảnh.");
                } else {
                    selectedImages.clear();
                    selectedImages.addAll(files);
                    lblImageInfo.setText(files.size() + " ảnh đã chọn");
                }
            }
        });

        Label lblCategory = new Label("Lĩnh vực phản ánh:");
        ComboBox<String> cbCategory = new ComboBox<>(
                FXCollections.observableArrayList(phanAnhService.getAvailableCategories()));
        cbCategory.getSelectionModel().selectFirst();
        cbCategory.setMaxWidth(Double.MAX_VALUE);

        Label lblTitle = new Label("Tiêu đề phản ánh:");
        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Nhập tiêu đề phản ánh");

        Label lblContent = new Label("Nội dung phản ánh:");
        TextArea txtContent = new TextArea();
        txtContent.setPromptText("Mô tả chi tiết phản ánh");
        txtContent.setPrefRowCount(5);

        grid.add(lblImages, 0, 0);
        grid.add(new HBox(10, btnUpload, lblImageInfo), 1, 0);
        grid.add(lblCategory, 0, 1);
        grid.add(cbCategory, 1, 1);
        grid.add(lblTitle, 0, 2);
        grid.add(txtTitle, 1, 2);
        grid.add(lblContent, 0, 3);
        grid.add(txtContent, 1, 3);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButtonType) {
            String title = txtTitle.getText().trim();
            String content = txtContent.getText().trim();
            String category = cbCategory.getValue();

            if (title.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Tiêu đề phản ánh là bắt buộc.");
                return;
            }
            if (content.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Nội dung phản ánh là bắt buộc.");
                return;
            }

            PhanAnh phanAnh = new PhanAnh();
            phanAnh.setTieuDe(title);
            phanAnh.setLinhVuc(category);
            phanAnh.setNoiDung(content);
            List<String> paths = new ArrayList<>();
            for (File file : selectedImages) {
                paths.add(file.getAbsolutePath());
            }
            phanAnh.setHinhAnh(paths);
            phanAnh.setNgayGui(LocalDate.now());
            phanAnh.setTrangThai("Chờ đợi");
            User currentUser = SessionManager.getInstance().getCurrentUser();
            phanAnh.setNguoiGui(currentUser != null ? currentUser.getUsername() : "Cư dân");

            if (phanAnhService.addReport(phanAnh)) {
                showAlert(Alert.AlertType.INFORMATION, "Thêm phản ánh thành công.");
                refreshTables();
            } else {
                showAlert(Alert.AlertType.ERROR, "Không thể thêm phản ánh. Vui lòng thử lại.");
            }
        }
    }

    private void showEditCitizenDialog(PhanAnh report) {
        if (report == null) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa phản ánh");
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createDialogGrid();
        Label lblTitle = new Label("Tiêu đề phản ánh:");
        TextField txtTitle = new TextField(report.getTieuDe());
        Label lblContent = new Label("Nội dung phản ánh:");
        TextArea txtContent = new TextArea(report.getNoiDung());
        txtContent.setPrefRowCount(5);

        grid.add(lblTitle, 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(lblContent, 0, 1);
        grid.add(txtContent, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String title = txtTitle.getText().trim();
            String content = txtContent.getText().trim();

            if (title.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Tiêu đề phản ánh là bắt buộc.");
                return;
            }
            if (content.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Nội dung phản ánh là bắt buộc.");
                return;
            }

            report.setTieuDe(title);
            report.setNoiDung(content);
            phanAnhService.updateReport(report);
            refreshTables();
            showAlert(Alert.AlertType.INFORMATION, "Cập nhật phản ánh thành công.");
        }
    }

    private void showAdminAssignDialog(PhanAnh report) {
        if (report == null) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tiếp nhận phản ánh");
        ButtonType confirmButtonType = new ButtonType("Xác nhận", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = createDialogGrid();
        Label lblAssignee = new Label("Người phụ trách:");
        ComboBox<String> cbAssignee = new ComboBox<>(
                FXCollections.observableArrayList(phanAnhService.getAvailableAssignees()));
        cbAssignee.setMaxWidth(Double.MAX_VALUE);
        cbAssignee.setValue(report.getNguoiPhuTrach() != null ? report.getNguoiPhuTrach() : "Ban quản lý");

        Label lblFeedback = new Label("Nội dung phản hồi:");
        TextArea txtFeedback = new TextArea(report.getPhanHoi());
        txtFeedback.setPrefRowCount(5);

        grid.add(lblAssignee, 0, 0);
        grid.add(cbAssignee, 1, 0);
        grid.add(lblFeedback, 0, 1);
        grid.add(txtFeedback, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmButtonType) {
            String assignee = cbAssignee.getValue();
            String feedback = txtFeedback.getText().trim();
            if (assignee == null || assignee.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn người phụ trách.");
                return;
            }

            report.setNguoiPhuTrach(assignee);
            report.setPhanHoi(feedback);
            report.setTrangThai("Đã tiếp nhận");
            phanAnhService.updateReport(report);

            try {
                com.bluemoon.services.ThongBaoService thongBaoService = new com.bluemoon.services.ThongBaoService();
                com.bluemoon.models.ThongBao tb = new com.bluemoon.models.ThongBao();
                tb.setTenThongBao("Phản ánh '" + report.getTieuDe() + "' đã được tiếp nhận bởi " + assignee);
                tb.setNgayBanHanh(LocalDate.now());
                tb.setTrangThai("Đã phát hành");
                
                com.bluemoon.services.PermissionService permService = new com.bluemoon.services.PermissionService();
                
                java.util.List<Integer> targetGroupIds = new java.util.ArrayList<>();
                for (com.bluemoon.models.UserGroupAssignment assignment : permService.findUserAssignments(report.getNguoiGui())) {
                    if (assignment.getUsername().equals(report.getNguoiGui()) && assignment.getGroupId() > 0) {
                        targetGroupIds.add(assignment.getGroupId());
                    }
                }
                
                // Nếu người gửi chưa được gán vào nhóm nào thì gửi cho tất cả
                if (targetGroupIds.isEmpty()) {
                    targetGroupIds = permService.findGroups(null).stream().map(g -> g.getId()).collect(java.util.stream.Collectors.toList());
                }
                
                thongBaoService.addNotification(tb, targetGroupIds);
            } catch (Exception e) {
                System.err.println("Lỗi gửi thông báo: " + e.getMessage());
            }

            refreshTables();
            showAlert(Alert.AlertType.INFORMATION, "Phản ánh đã được cập nhật và chuyển trạng thái.");
        }
    }

    private void confirmDelete(PhanAnh report) {
        if (report == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa phản ánh");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa phản ánh này không?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            phanAnhService.deleteReport(report.getId());
            refreshTables();
            showAlert(Alert.AlertType.INFORMATION, "Đã xóa phản ánh.");
        }
    }

    private GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));
        grid.getColumnConstraints().addAll();
        return grid;
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


