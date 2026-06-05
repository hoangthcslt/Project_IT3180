package com.bluemoon.controllers;

import com.bluemoon.models.User;
import com.bluemoon.models.HoKhau;
import com.bluemoon.models.KhoanThu;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.models.PaymentStatusView;
import com.bluemoon.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import com.bluemoon.services.FeeService;
import com.bluemoon.services.HouseholdService;
import com.bluemoon.services.PaymentService;
import com.bluemoon.services.PermissionService;
import com.bluemoon.services.ResidentService;
import com.bluemoon.models.ThongBao;
import com.bluemoon.services.ThongBaoService;
import com.bluemoon.utils.SessionManager;
import java.io.File;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class DashboardController {

    @FXML
    private MenuButton menuUser;

    @FXML
    private MenuButton menuNotifications;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button btnTrangChu;
    @FXML
    private Button btnHoKhau;
    @FXML
    private Button btnNhanKhau;
    @FXML
    private Button btnKhoanThu;
    @FXML
    private Button btnNopTien;
    @FXML
    private Button btnThongKe;
    @FXML
    private Button btnPhanQuyen;
    @FXML
    private Button btnTienIch;
    @FXML
    private Button btnPhanAnh;

    private Node dashboardView;
    private Label lblHouseholdTotal;
    private Label lblResidentTotal;
    private Label lblFeeTotal;
    private Label lblPaidTotal;
    private Label lblUnpaidTotal;
    private final HouseholdService householdService = new HouseholdService();
    private final ResidentService residentService = new ResidentService();
    private final FeeService feeService = new FeeService();
    private final PaymentService paymentService = new PaymentService();
    private final PermissionService permissionService = new PermissionService();
    private final ThongBaoService thongBaoService = new ThongBaoService();

    @FXML
    public void initialize() {
        dashboardView = createHomeDashboard();
        mainBorderPane.setCenter(dashboardView);
        refreshDashboardStats();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            Circle avatar = new Circle(14);
            try {
                Image img = new Image(getClass().getResourceAsStream("/images/avatar.png"));
                avatar.setFill(new ImagePattern(img));
            } catch (Exception e) {
                avatar.setFill(Color.web("#ecf0f1"));
            }
            Label nameLabel = new Label("  " + currentUser.getUsername());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            HBox graphicBox = new HBox(5, avatar, nameLabel);
            graphicBox.setAlignment(Pos.CENTER_LEFT);
            menuUser.setGraphic(graphicBox);
            menuUser.setText("");
        }
        refreshNotifications();
    }

    private void loadView(String fxml) {
        System.out.println("[DEBUG] loadView called for: " + fxml);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxml));
            Node view = loader.load();
            mainBorderPane.setCenter(view);
            if (loader.getController() instanceof ThongBaoController) {
                ((ThongBaoController) loader.getController()).setDashboardController(this);
            }
            System.out.println("[DEBUG] Open " + fxml + " success");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load view: " + fxml);
            e.printStackTrace();
        }
    }

    @FXML
    void handleTrangChu(ActionEvent event) {
        refreshDashboardStats();
        mainBorderPane.setCenter(dashboardView);
    }

    @FXML
    void handlePhanAnh(ActionEvent event) {
        loadView("phananh.fxml");
    }

    private Node createHomeDashboard() {
        BorderPane wrapper = new BorderPane();
        wrapper.setStyle("-fx-background-color: #f4f7fb;");
        wrapper.setPadding(new Insets(30));

        VBox header = new VBox(6);
        Label title = new Label("Tổng quan hệ thống");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #243447;");
        Label subtitle = new Label("Theo dõi nhanh dữ liệu quản lý chung cư BlueMoon");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7a8f;");
        header.getChildren().addAll(title, subtitle);
        BorderPane.setMargin(header, new Insets(0, 0, 22, 0));
        wrapper.setTop(header);

        FlowPane cards = new FlowPane();
        cards.setHgap(18);
        cards.setVgap(18);
        cards.setPrefWrapLength(900);
        cards.setAlignment(Pos.TOP_LEFT);

        lblHouseholdTotal = createMetricLabel();
        lblResidentTotal = createMetricLabel();
        lblFeeTotal = createMetricLabel();
        lblPaidTotal = createMetricLabel();
        lblUnpaidTotal = createSmallMetricLabel();

        cards.getChildren().addAll(
                createDashboardCard("Hộ khẩu", lblHouseholdTotal, null, "🏠", "#2f80ed", this::showHouseholdListView),
                createDashboardCard("Nhân khẩu", lblResidentTotal, null, "👥", "#27ae60", this::showResidentListView),
                createDashboardCard("Khoản thu", lblFeeTotal, null, "💰", "#f2994a", this::showFeeListView),
                createDashboardCard("Nộp tiền", lblPaidTotal, lblUnpaidTotal, "💵", "#9b51e0",
                        this::showPaymentStatusView),
                createDashboardCard("Thống kê", null, null, "📊", "#eb5757", () -> loadView("thongke.fxml")));

        VBox centerContent = new VBox(28, cards);
        centerContent.setAlignment(Pos.TOP_LEFT);
        wrapper.setCenter(centerContent);
        return wrapper;
    }

    private VBox createDashboardCard(String title, Label mainMetric, Label subMetric, String icon, String color,
            Runnable action) {
        VBox card = new VBox(14);
        card.setPrefSize(215, 170);
        card.setMinSize(215, 170);
        card.setMaxSize(215, 170);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(35, 52, 72, 0.13), 14, 0, 0, 5);");

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(48, 48);
        iconBox.setMinSize(48, 48);
        iconBox.setMaxSize(48, 48);
        iconBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 14;");
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        iconBox.getChildren().add(iconLabel);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #263238;");
        top.getChildren().addAll(iconBox, titleLabel);

        VBox metricBox = new VBox(4);
        metricBox.setMinHeight(52);
        if (mainMetric != null) {
            metricBox.getChildren().add(mainMetric);
        } else {
            Label summary = new Label("Báo cáo tổng hợp");
            summary.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #263238;");
            metricBox.getChildren().add(summary);
        }
        if (subMetric != null) {
            metricBox.getChildren().add(subMetric);
        }

        Button viewButton = new Button("Xem danh sách");
        viewButton.setMaxWidth(Double.MAX_VALUE);
        viewButton.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 9;");
        viewButton.setOnAction(event -> action.run());

        card.setOnMouseEntered(event -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(35, 52, 72, 0.22), 18, 0, 0, 7);"));
        card.setOnMouseExited(event -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(35, 52, 72, 0.13), 14, 0, 0, 5);"));
        viewButton.setOnMouseEntered(event -> viewButton.setStyle("-fx-background-color: derive(" + color
                + ", -12%); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 9;"));
        viewButton.setOnMouseExited(event -> viewButton.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 9;"));

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(top, metricBox, spacer, viewButton);
        return card;
    }

    private void showHouseholdListView() {
        TableView<HoKhau> table = createReadOnlyTable();
        table.getColumns().add(column("ID", "id", 50));
        table.getColumns().add(column("Mã hộ khẩu", "maHoKhau", 130));
        table.getColumns().add(column("Tên chủ hộ", "tenChuHo", 180));
        table.getColumns().add(column("Diện tích", "dienTich", 100));
        table.getColumns().add(column("Số xe máy", "soXeMay", 100));
        table.getColumns().add(column("Số ô tô", "soOto", 100));
        table.getColumns().add(column("Ngày lập", "ngayLap", 120));
        table.getItems().setAll(householdService.getAllHouseholds());
        mainBorderPane.setCenter(
                createReadOnlyPage("Danh sách hộ khẩu", "Dữ liệu được tải trực tiếp từ chức năng Hộ khẩu.", table));
    }

    private void showResidentListView() {
        TableView<NhanKhau> table = createReadOnlyTable();
        table.getColumns().add(column("ID", "id", 50));
        table.getColumns().add(column("Họ tên", "hoTen", 180));
        table.getColumns().add(column("Ngày sinh", "ngaySinh", 110));
        table.getColumns().add(column("Giới tính", "gioiTinh", 80));
        table.getColumns().add(column("CCCD", "cccd", 130));
        table.getColumns().add(column("Số điện thoại", "soDienThoai", 120));
        table.getColumns().add(column("Quan hệ", "quanHe", 120));
        table.getColumns().add(column("Hộ khẩu", "hoKhauId", 90));
        table.getColumns().add(column("Trạng thái", "trangThai", 110));
        table.getItems().setAll(residentService.getAllResidents());
        mainBorderPane.setCenter(
                createReadOnlyPage("Danh sách nhân khẩu", "Dữ liệu được tải trực tiếp từ chức năng Nhân khẩu.", table));
    }

    private void showFeeListView() {
        TableView<KhoanThu> table = createReadOnlyTable();
        table.getColumns().add(column("ID", "id", 50));
        table.getColumns().add(column("Mã khoản thu", "maKhoanThu", 120));
        table.getColumns().add(column("Tên khoản thu", "tenKhoanThu", 200));
        table.getColumns().add(column("Loại phí", "loaiPhi", 100));
        table.getColumns().add(column("Đơn giá", "donGia", 110));
        table.getColumns().add(column("Ngày tạo", "ngayTao", 110));
        table.getColumns().add(column("Hạn nộp", "hanNop", 110));
        table.getColumns().add(column("Trạng thái", "trangThai", 110));
        table.getItems().setAll(feeService.getAllFees());
        mainBorderPane.setCenter(
                createReadOnlyPage("Danh sách khoản thu", "Dữ liệu được tải trực tiếp từ chức năng Khoản thu.", table));
    }

    private void showPaymentStatusView() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(createPaymentTab("Danh sách đã nộp", true));
        tabs.getTabs().add(createPaymentTab("Danh sách chưa nộp", false));
        mainBorderPane.setCenter(createReadOnlyPage("Quản lý trạng thái nộp tiền",
                "Theo dõi trạng thái thanh toán theo từng hộ và khoản thu.", tabs));
    }

    private Tab createPaymentTab(String title, boolean paid) {
        TableView<PaymentStatusView> table = createReadOnlyTable();
        table.getColumns().add(column("ID", "id", 70));
        table.getColumns().add(column("Hộ khẩu", "maHoKhau", 130));
        table.getColumns().add(column("Tên chủ hộ", "tenChuHo", 190));
        table.getColumns().add(column("Khoản thu", "tenKhoanThu", 220));
        table.getColumns().add(column("Số tiền", "soTien", 130));
        table.getColumns().add(column(paid ? "Ngày nộp" : "Hạn đóng", paid ? "ngayNop" : "hanDong", 130));
        table.getColumns().add(column("Trạng thái", "trangThai", 130));

        TextField search = new TextField();
        search.setPromptText(
                paid ? "Tìm theo mã hộ, chủ hộ, khoản thu, trạng thái" : "Tìm theo mã hộ, chủ hộ, khoản thu");
        search.setStyle("-fx-background-radius: 8; -fx-padding: 9 12;");
        Consumer<String> loader = keyword -> table.getItems().setAll(
                paid ? paymentService.findPaidItems(keyword) : paymentService.findUnpaidItems(keyword));
        search.textProperty().addListener((obs, oldText, newText) -> loader.accept(newText));
        loader.accept("");

        VBox content = new VBox(12, search, table);
        content.setPadding(new Insets(14));
        VBox.setVgrow(table, Priority.ALWAYS);
        return new Tab(title, content);
    }

    private <T> TableView<T> createReadOnlyTable() {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setEditable(false);
        table.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dfe7f1; -fx-border-radius: 8;");
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private <S, T> TableColumn<S, T> column(String title, String property, double width) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    private Node createReadOnlyPage(String title, String subtitle, Node content) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        page.setStyle("-fx-background-color: #f4f7fb;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #243447;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7a8f;");

        VBox body = new VBox(content);
        body.setPadding(new Insets(16));
        body.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(35, 52, 72, 0.12), 14, 0, 0, 5);");
        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(body, Priority.ALWAYS);

        page.getChildren().addAll(titleLabel, subtitleLabel, body);
        return page;
    }

    private Label createMetricLabel() {
        Label label = new Label();
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #263238;");
        return label;
    }

    private Label createSmallMetricLabel() {
        Label label = new Label();
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        return label;
    }

    private void refreshDashboardStats() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            applyUserPermissions(currentUser);
        }
        lblHouseholdTotal.setText(householdService.getAllHouseholds().size() + " Hộ khẩu");
        lblResidentTotal.setText(residentService.getAllResidents().size() + " Nhân khẩu");
        lblFeeTotal.setText(feeService.getAllFees().size() + " Khoản thu");
        lblPaidTotal.setText(paymentService.countPaidItems() + " Đã nộp");
        lblUnpaidTotal.setText(paymentService.countUnpaidItems() + " Chưa nộp");
        refreshNotifications();
    }

    @FXML
    void handleHoKhau(ActionEvent event) {
        loadView("hokhau.fxml");
    }

    @FXML
    void handleNhanKhau(ActionEvent event) {
        loadView("nhankhau.fxml");
    }

    @FXML
    void handleKhoanThu(ActionEvent event) {
        System.out.println("[DEBUG] handleKhoanThu invoked");
        loadView("khoanthu.fxml");
    }

    @FXML
    void handleNopTien(ActionEvent event) {
        loadView("thanhtoan.fxml");
    }

    @FXML
    void handleThongKe(ActionEvent event) {
        loadView("thongke.fxml");
    }

    @FXML
    void handlePhanQuyen(ActionEvent event) {
        loadView("phanquyen.fxml");
    }

    @FXML
    void handleTienIch(ActionEvent event) {
        loadView("thongbao.fxml");
    }

    private void applyUserPermissions(User user) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            showAllPermissionButtons();
            return;
        }
        if (!permissionService.hasUserGroup(user.getId())) {
            return;
        }

        Set<String> permissions = permissionService.getPermissionCodesByUser(user.getId());
        Map<String, Button> permissionButtons = Map.of(
                "TRANG_CHU", btnTrangChu,
                "HO_KHAU", btnHoKhau,
                "NHAN_KHAU", btnNhanKhau,
                "KHOAN_THU", btnKhoanThu,
                "NOP_TIEN", btnNopTien,
                "THONG_KE", btnThongKe,
                "PHAN_QUYEN", btnPhanQuyen, "TIEN_ICH", btnTienIch);

        permissionButtons.forEach((code, button) -> {
            boolean allowed = permissions.contains(code);
            button.setVisible(allowed);
            button.setManaged(allowed);
        });

        boolean canPhanAnh = permissions.contains("PHAN_ANH_GUI") || permissions.contains("PHAN_ANH_TIEP_NHAN");
        btnPhanAnh.setVisible(canPhanAnh);
        btnPhanAnh.setManaged(canPhanAnh);
    }

    private void showAllPermissionButtons() {
        btnTrangChu.setVisible(true);
        btnTrangChu.setManaged(true);
        btnHoKhau.setVisible(true);
        btnHoKhau.setManaged(true);
        btnNhanKhau.setVisible(true);
        btnNhanKhau.setManaged(true);
        btnKhoanThu.setVisible(true);
        btnKhoanThu.setManaged(true);
        btnNopTien.setVisible(true);
        btnNopTien.setManaged(true);
        btnThongKe.setVisible(true);
        btnThongKe.setManaged(true);
        btnPhanQuyen.setVisible(true);
        btnPhanQuyen.setManaged(true);
        btnTienIch.setVisible(true);
        btnTienIch.setManaged(true);
        btnPhanAnh.setVisible(true);
        btnPhanAnh.setManaged(true);
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Nhập mật khẩu mới cho tài khoản của bạn.");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Mật khẩu hiện tại");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("Mật khẩu mới");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Xác nhận mật khẩu mới");

        grid.add(new Label("Mật khẩu hiện tại:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 2);
        grid.add(confirmPassword, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (newPassword.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Mật khẩu mới không được để trống!");
                    alert.showAndWait();
                    return null;
                }
                if (newPassword.getText().equals(confirmPassword.getText())) {
                    return newPassword.getText();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Mật khẩu xác nhận không khớp!");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            User user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                UserRepository repo = new UserRepository();
                if (repo.updatePassword(user.getId(), hashed)) {
                    user.setPassword(hashed); // Cập nhật session
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText("Mật khẩu đã được cập nhật thành công vào CSDL!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Lỗi khi lưu vào cơ sở dữ liệu.");
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có muốn đăng xuất?");

        ButtonType btnCo = new ButtonType("Có");
        ButtonType btnKhong = new ButtonType("Không");
        alert.getButtonTypes().setAll(btnCo, btnKhong);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnCo) {
            try {
                SessionManager.getInstance().clearSession();
                Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
                stage.setTitle("Hệ thống quản lý chung cư BlueMoon - Đăng nhập");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && menuNotifications != null) {
            List<ThongBao> list = thongBaoService.getNotificationsForUser(currentUser.getId());
            List<Integer> readIds = thongBaoService.getReadNotificationIds(currentUser.getId());
            long unreadCount = list.stream().filter(tb -> !readIds.contains(tb.getId())).count();

            menuNotifications.getItems().clear();

            HBox graphicBox = new HBox(6);
            graphicBox.setAlignment(Pos.CENTER);

            SVGPath bellIcon = new SVGPath();
            bellIcon.setContent(
                    "M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.002-12a4.5 4.5 0 0 0-4.498 4.498v3.504l-1 1v1h11v-1l-1-1V8.5A4.5 4.5 0 0 0 8.002 4z");
            bellIcon.setFill(Color.WHITE);

            Label textLabel = new Label("Thông báo");
            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            graphicBox.getChildren().addAll(bellIcon, textLabel);

            if (unreadCount > 0) {
                StackPane badge = new StackPane();
                badge.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 10px; -fx-padding: 2 6;");
                Label badgeLabel = new Label(String.valueOf(unreadCount));
                badgeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
                badge.getChildren().add(badgeLabel);
                graphicBox.getChildren().add(badge);
            }

            menuNotifications.setGraphic(graphicBox);
            menuNotifications.setText("");

            if (list.isEmpty()) {
                MenuItem item = new MenuItem("Không có thông báo mới");
                item.setDisable(true);
                menuNotifications.getItems().add(item);
            } else {
                if (unreadCount > 0) {
                    MenuItem markAllReadItem = new MenuItem("✓ Đánh dấu tất cả là đã đọc");
                    markAllReadItem.setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e;");
                    markAllReadItem.setOnAction(evt -> {
                        for (ThongBao tb : list) {
                            if (!readIds.contains(tb.getId())) {
                                thongBaoService.markAsRead(currentUser.getId(), tb.getId());
                            }
                        }
                        refreshNotifications();
                    });
                    menuNotifications.getItems().add(markAllReadItem);
                    menuNotifications.getItems().add(new SeparatorMenuItem());
                }

                for (ThongBao tb : list) {
                    boolean isRead = readIds.contains(tb.getId());
                    String title = tb.getTenThongBao();
                    if (title.length() > 30) {
                        title = title.substring(0, 27) + "...";
                    }
                    String statusSuffix = isRead ? "" : " (Mới)";
                    MenuItem item = new MenuItem(title + statusSuffix + " (" + tb.getNgayBanHanh() + ")");
                    if (isRead) {
                        item.setStyle("-fx-text-fill: #94a3b8;");
                    } else {
                        item.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
                    }
                    item.setOnAction(evt -> {
                        thongBaoService.markAsRead(currentUser.getId(), tb.getId());
                        showNotificationDetail(tb);
                        refreshNotifications();
                    });
                    menuNotifications.getItems().add(item);
                }
            }
        }
    }

    private void showNotificationDetail(ThongBao tb) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết thông báo");
        alert.setHeaderText(tb.getTenThongBao());

        StringBuilder sb = new StringBuilder();
        sb.append("Ngày ban hành: ").append(tb.getNgayBanHanh()).append("\n");
        if (tb.getFilePath() != null && !tb.getFilePath().isBlank()) {
            File f = new File(tb.getFilePath());
            sb.append("File đính kèm: ").append(f.getName()).append("\n");
            sb.append("Đường dẫn file: ").append(tb.getFilePath());
        } else {
            sb.append("File đính kèm: Không có");
        }

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
}
