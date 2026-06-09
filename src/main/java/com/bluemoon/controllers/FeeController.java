package com.bluemoon.controllers;

import com.bluemoon.models.DanhMucPhi;
import com.bluemoon.models.KhoanThu;
import com.bluemoon.models.HoaDon;
import com.bluemoon.models.ChiTietHoaDon;
import com.bluemoon.services.DanhMucPhiService;
import com.bluemoon.services.FeeService;
import com.bluemoon.services.HoaDonService;
import com.bluemoon.repositories.FeeRepository;
import com.bluemoon.repositories.HoaDonRepository;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeeController {

    // Tab 2: Mandatory Fees
    @FXML private TableView<DanhMucPhi> tableMandatoryFees;
    @FXML private TableColumn<DanhMucPhi, Integer> colMandatoryId;
    @FXML private TableColumn<DanhMucPhi, String> colMandatoryCode;
    @FXML private TableColumn<DanhMucPhi, String> colMandatoryName;
    @FXML private TableColumn<DanhMucPhi, String> colMandatoryCalcType;
    @FXML private TableColumn<DanhMucPhi, BigDecimal> colMandatoryPrice;
    @FXML private TableColumn<DanhMucPhi, String> colMandatoryNote;
    @FXML private TableColumn<DanhMucPhi, Void> colMandatoryActions;
    @FXML private TextField txtSearchMandatoryFee;

    // Tab 3: Voluntary Fees
    @FXML private TableView<DanhMucPhi> tableVoluntaryFees;
    @FXML private TableColumn<DanhMucPhi, Integer> colVoluntaryId;
    @FXML private TableColumn<DanhMucPhi, String> colVoluntaryCode;
    @FXML private TableColumn<DanhMucPhi, String> colVoluntaryName;
    @FXML private TableColumn<DanhMucPhi, String> colVoluntaryNote;
    @FXML private TableColumn<DanhMucPhi, Void> colVoluntaryActions;
    @FXML private TextField txtSearchVoluntaryFee;

    // Tab 1: Billing Runs
    @FXML private TableView<KhoanThu> tableBillingRuns;
    @FXML private TableColumn<KhoanThu, Integer> colRunId;
    @FXML private TableColumn<KhoanThu, String> colRunCode;
    @FXML private TableColumn<KhoanThu, String> colRunName;
    @FXML private TableColumn<KhoanThu, LocalDate> colRunCreatedDate;
    @FXML private TableColumn<KhoanThu, LocalDate> colRunDueDate;
    @FXML private TableColumn<KhoanThu, String> colRunStatus;
    @FXML private TableColumn<KhoanThu, Void> colRunActions;
    @FXML private TextField txtSearchRun;

    private DanhMucPhiService fixedFeeService;
    private FeeService billingRunService;
    private HoaDonService invoiceService;
    private FeeRepository feeRepository;

    private ObservableList<DanhMucPhi> mandatoryFeeList;
    private ObservableList<DanhMucPhi> voluntaryFeeList;
    private ObservableList<KhoanThu> billingRunList;

    @FXML
    public void initialize() {
        fixedFeeService = new DanhMucPhiService();
        billingRunService = new FeeService();
        invoiceService = new HoaDonService();
        feeRepository = new FeeRepository();

        mandatoryFeeList = FXCollections.observableArrayList();
        voluntaryFeeList = FXCollections.observableArrayList();
        billingRunList = FXCollections.observableArrayList();

        // 1. Setup Tab 2: Mandatory Fees Columns
        colMandatoryId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colMandatoryCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaPhi()));
        colMandatoryName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenPhi()));
        colMandatoryCalcType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLoaiTinhGia()));
        colMandatoryPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDonGia()));
        colMandatoryPrice.setCellFactory(col -> new TableCell<DanhMucPhi, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    DanhMucPhi phi = getTableRow().getItem();
                    if ("DIEN".equals(phi.getMaPhi())) {
                        setText("Theo bậc thang");
                    } else {
                        setText(item != null ? item.toString() : "0");
                    }
                }
            }
        });
        colMandatoryNote.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGhiChu()));
        colMandatoryNote.setCellFactory(col -> new TableCell<DanhMucPhi, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    DanhMucPhi phi = getTableRow().getItem();
                    if ("DIEN".equals(phi.getMaPhi())) {
                        setText("Tiền điện hàng tháng tính theo bậc thang");
                    } else {
                        setText(item);
                    }
                }
            }
        });
        colMandatoryActions.setCellFactory(col -> actionCellMandatoryFees());

        // 2. Setup Tab 3: Voluntary Fees Columns
        colVoluntaryId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colVoluntaryCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaPhi()));
        colVoluntaryName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenPhi()));
        colVoluntaryNote.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGhiChu()));
        colVoluntaryActions.setCellFactory(col -> actionCellVoluntaryFees());

        // 3. Setup Tab 1: Billing Runs Columns
        colRunId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colRunCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaKhoanThu()));
        colRunName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenKhoanThu()));
        colRunCreatedDate.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getNgayTao()));
        colRunDueDate.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getHanNop()));
        colRunStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colRunActions.setCellFactory(col -> actionCellBillingRuns());

        tableMandatoryFees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableVoluntaryFees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableBillingRuns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load data
        loadFixedFees();
        loadBillingRuns();
    }

    private void loadFixedFees() {
        List<DanhMucPhi> allFees = fixedFeeService.getAllDanhMucPhi();
        List<DanhMucPhi> mandatory = new ArrayList<>();
        List<DanhMucPhi> voluntary = new ArrayList<>();
        for (DanhMucPhi f : allFees) {
            if ("BAT_BUOC".equals(f.getLoaiPhi())) {
                mandatory.add(f);
            } else if ("TU_NGUYEN".equals(f.getLoaiPhi())) {
                voluntary.add(f);
            }
        }
        mandatory.sort((f1, f2) -> {
            if ("DIEN".equals(f1.getMaPhi())) return -1;
            if ("DIEN".equals(f2.getMaPhi())) return 1;
            return f1.getMaPhi().compareTo(f2.getMaPhi());
        });
        mandatoryFeeList.setAll(mandatory);
        voluntaryFeeList.setAll(voluntary);
        tableMandatoryFees.setItems(mandatoryFeeList);
        tableVoluntaryFees.setItems(voluntaryFeeList);
    }

    private void loadBillingRuns() {
        billingRunList.setAll(billingRunService.getAllFees());
        tableBillingRuns.setItems(billingRunList);
    }

    // --- TAB 2 & 3 ACTIONS ---

    @FXML
    void handleShowAddMandatoryFee(ActionEvent event) {
        showMandatoryFeeDialog(null);
    }

    @FXML
    void handleSearchMandatoryFee(ActionEvent event) {
        String kw = txtSearchMandatoryFee.getText();
        if (kw == null || kw.trim().isEmpty()) {
            loadFixedFees();
            return;
        }
        mandatoryFeeList.setAll(fixedFeeService.searchDanhMucPhi(kw, kw, "BAT_BUOC", null));
    }

    @FXML
    void handleResetMandatoryFee(ActionEvent event) {
        txtSearchMandatoryFee.clear();
        loadFixedFees();
    }

    @FXML
    void handleShowAddVoluntaryFee(ActionEvent event) {
        showVoluntaryFeeDialog(null);
    }

    @FXML
    void handleSearchVoluntaryFee(ActionEvent event) {
        String kw = txtSearchVoluntaryFee.getText();
        if (kw == null || kw.trim().isEmpty()) {
            loadFixedFees();
            return;
        }
        voluntaryFeeList.setAll(fixedFeeService.searchDanhMucPhi(kw, kw, "TU_NGUYEN", null));
    }

    @FXML
    void handleResetVoluntaryFee(ActionEvent event) {
        txtSearchVoluntaryFee.clear();
        loadFixedFees();
    }

    private void showMandatoryFeeDialog(DanhMucPhi item) {
        boolean adding = (item == null);
        if (!adding && "DIEN".equals(item.getMaPhi())) {
            showDienPricingDialog(item);
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(adding ? "Thêm phí bắt buộc mới" : "Chỉnh sửa phí bắt buộc");
        dialog.setHeaderText(null);

        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtCode = new TextField(adding ? "" : item.getMaPhi());
        if (!adding) txtCode.setDisable(true); // Can't change code after creation
        TextField txtName = new TextField(adding ? "" : item.getTenPhi());
        
        ComboBox<String> cbCalc = new ComboBox<>(FXCollections.observableArrayList("CO_DINH", "THEO_DIEN_TICH", "THEO_SO_NGUOI", "NHAP_TAY"));
        cbCalc.setValue(adding ? "CO_DINH" : item.getLoaiTinhGia());

        TextField txtPrice = new TextField(adding ? "0" : item.getDonGia().toString());
        TextField txtNote = new TextField(adding ? "" : item.getGhiChu());

        grid.add(new Label("Mã phí:"), 0, 0);
        grid.add(txtCode, 1, 0);
        grid.add(new Label("Tên phí:"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Loại tính giá:"), 0, 2);
        grid.add(cbCalc, 1, 2);
        grid.add(new Label("Đơn giá (VND):"), 0, 3);
        grid.add(txtPrice, 1, 3);
        grid.add(new Label("Ghi chú:"), 0, 4);
        grid.add(txtNote, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Validation helper
        txtPrice.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) return change;
            return null;
        }));

        final Button btnOk = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        btnOk.addEventFilter(ActionEvent.ACTION, ae -> {
            if (txtCode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty() || txtPrice.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
                ae.consume();
                return;
            }
            if (adding && fixedFeeService.isMaPhiExists(txtCode.getText().trim())) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã phí đã tồn tại trong hệ thống!");
                ae.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            try {
                DanhMucPhi phi = adding ? new DanhMucPhi() : item;
                phi.setMaPhi(txtCode.getText().trim().toUpperCase());
                phi.setTenPhi(txtName.getText().trim());
                phi.setLoaiPhi("BAT_BUOC");
                phi.setLoaiTinhGia(cbCalc.getValue());
                phi.setDonGia(new BigDecimal(txtPrice.getText().trim()));
                phi.setGhiChu(txtNote.getText().trim());

                boolean success = adding ? fixedFeeService.addDanhMucPhi(phi) : fixedFeeService.updateDanhMucPhi(phi);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu thông tin phí bắt buộc thành công.");
                    loadFixedFees();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu thông tin phí vào CSDL.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + e.getMessage());
            }
        }
    }

    private void showVoluntaryFeeDialog(DanhMucPhi item) {
        boolean adding = (item == null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(adding ? "Thêm phí tự nguyện mới" : "Chỉnh sửa phí tự nguyện");
        dialog.setHeaderText(null);

        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtCode = new TextField(adding ? "" : item.getMaPhi());
        if (!adding) txtCode.setDisable(true); // Can't change code after creation
        TextField txtName = new TextField(adding ? "" : item.getTenPhi());
        TextField txtNote = new TextField(adding ? "" : item.getGhiChu());

        grid.add(new Label("Mã phí:"), 0, 0);
        grid.add(txtCode, 1, 0);
        grid.add(new Label("Tên phí:"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Ghi chú:"), 0, 2);
        grid.add(txtNote, 1, 2);

        dialog.getDialogPane().setContent(grid);

        final Button btnOk = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        btnOk.addEventFilter(ActionEvent.ACTION, ae -> {
            if (txtCode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
                ae.consume();
                return;
            }
            if (adding && fixedFeeService.isMaPhiExists(txtCode.getText().trim())) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã phí đã tồn tại trong hệ thống!");
                ae.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            try {
                DanhMucPhi phi = adding ? new DanhMucPhi() : item;
                phi.setMaPhi(txtCode.getText().trim().toUpperCase());
                phi.setTenPhi(txtName.getText().trim());
                phi.setLoaiPhi("TU_NGUYEN");
                phi.setLoaiTinhGia("CO_DINH"); // Default type
                phi.setDonGia(BigDecimal.ZERO); // Default 0
                phi.setGhiChu(txtNote.getText().trim());

                boolean success = adding ? fixedFeeService.addDanhMucPhi(phi) : fixedFeeService.updateDanhMucPhi(phi);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu thông tin phí tự nguyện thành công.");
                    loadFixedFees();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu thông tin phí vào CSDL.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + e.getMessage());
            }
        }
    }

    private void handleDeleteFixedFee(DanhMucPhi item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa phí '" + item.getTenPhi() + "'?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (fixedFeeService.deleteDanhMucPhi(item.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa phí.");
                loadFixedFees();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa phí. Có thể có dữ liệu liên kết.");
            }
        }
    }

    private TableCell<DanhMucPhi, Void> actionCellMandatoryFees() {
        return new TableCell<>() {
            private final Button editBtn = createActionButton("✎");
            private final Button deleteBtn = createActionButton("🗑");
            private final HBox box = new HBox(8, editBtn, deleteBtn);
            {
                box.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showMandatoryFeeDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteFixedFee(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DanhMucPhi phi = getTableView().getItems().get(getIndex());
                    if ("DIEN".equals(phi.getMaPhi())) {
                        box.getChildren().setAll(editBtn);
                    } else {
                        box.getChildren().setAll(editBtn, deleteBtn);
                    }
                    setGraphic(box);
                }
                setAlignment(Pos.CENTER);
            }
        };
    }

    private TableCell<DanhMucPhi, Void> actionCellVoluntaryFees() {
        return new TableCell<>() {
            private final Button editBtn = createActionButton("✎");
            private final Button deleteBtn = createActionButton("🗑");
            private final HBox box = new HBox(8, editBtn, deleteBtn);
            {
                box.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showVoluntaryFeeDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteFixedFee(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setAlignment(Pos.CENTER);
            }
        };
    }

    // --- TAB 2 ACTIONS ---

    @FXML
    void handleShowNewBillingRun(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tạo đợt thu phí & Hóa đơn mới");
        dialog.setHeaderText(null);

        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtCode = new TextField("");
        txtCode.setPromptText("vd: DOT_202606");
        TextField txtName = new TextField("");
        txtName.setPromptText("vd: Đợt thu phí Tháng 06/2026");

        DatePicker dpCreated = new DatePicker(LocalDate.now());
        DatePicker dpDue = new DatePicker(LocalDate.now().plusDays(15));
        TextField txtNote = new TextField("");

        grid.add(new Label("Mã đợt thu:"), 0, 0);
        grid.add(txtCode, 1, 0);
        grid.add(new Label("Tên đợt thu:"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Ngày tạo:"), 0, 2);
        grid.add(dpCreated, 1, 2);
        grid.add(new Label("Hạn nộp:"), 0, 3);
        grid.add(dpDue, 1, 3);
        grid.add(new Label("Ghi chú:"), 0, 4);
        grid.add(txtNote, 1, 4);

        dialog.getDialogPane().setContent(grid);

        final Button btnOk = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        btnOk.addEventFilter(ActionEvent.ACTION, ae -> {
            if (txtCode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty() || dpCreated.getValue() == null || dpDue.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ các trường thông tin!");
                ae.consume();
                return;
            }
            if (billingRunService.isMaKhoanThuExists(txtCode.getText().trim())) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã đợt thu này đã tồn tại!");
                ae.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            try {
                KhoanThu kt = new KhoanThu(
                        0,
                        txtCode.getText().trim().toUpperCase(),
                        txtName.getText().trim(),
                        "BAT_BUOC",
                        BigDecimal.ZERO,
                        dpCreated.getValue(),
                        txtNote.getText().trim(),
                        dpDue.getValue(),
                        "DRAFT"
                );
                if (billingRunService.createFee(kt)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đợt thu nháp đã được khởi tạo và tự động tạo hóa đơn nháp cho tất cả các hộ dân!");
                    loadBillingRuns();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo đợt thu phí.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleSearchRun(ActionEvent event) {
        String kw = txtSearchRun.getText();
        if (kw == null || kw.trim().isEmpty()) {
            loadBillingRuns();
            return;
        }
        billingRunList.setAll(billingRunService.searchFees(kw, kw, null, null, null));
    }

    @FXML
    void handleResetRun(ActionEvent event) {
        txtSearchRun.clear();
        loadBillingRuns();
    }

    private void handleInputReadings(KhoanThu run) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nhập số liệu chỉ số - " + run.getTenKhoanThu());
        dialog.setHeaderText("Nhập các chỉ số tiêu thụ cần nhập tay cho các hộ dân.\nNhấn Lưu toàn bộ để cập nhật tất cả hóa đơn.");
        dialog.getDialogPane().setMinWidth(800);
        dialog.getDialogPane().setMinHeight(500);

        ButtonType saveButtonType = new ButtonType("Lưu toàn bộ", ButtonData.OK_DONE);
        ButtonType closeButtonType = new ButtonType("Hủy", ButtonData.CANCEL_CLOSE);
        
        boolean isDraft = "DRAFT".equals(run.getTrangThai());
        if (isDraft) {
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, closeButtonType);
        } else {
            dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Đóng", ButtonData.CANCEL_CLOSE));
        }

        // Get all invoices for this run
        List<HoaDon> invoices = invoiceService.getInvoicesByRun(run.getId());

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));

        // Headers
        Label lblColHk = new Label("Căn Hộ");
        Label lblColOwner = new Label("Chủ Hộ");
        String headerStyle = "-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;";
        lblColHk.setStyle(headerStyle);
        lblColOwner.setStyle(headerStyle);

        grid.add(lblColHk, 0, 0);
        grid.add(lblColOwner, 1, 0);

        List<DanhMucPhi> nhapTayFees = fixedFeeService.getAllDanhMucPhi().stream()
                .filter(f -> "BAT_BUOC".equals(f.getLoaiPhi()) && "NHAP_TAY".equals(f.getLoaiTinhGia()))
                .sorted((f1, f2) -> {
                    if ("DIEN".equals(f1.getMaPhi())) return -1;
                    if ("DIEN".equals(f2.getMaPhi())) return 1;
                    return f1.getMaPhi().compareTo(f2.getMaPhi());
                })
                .collect(java.util.stream.Collectors.toList());

        int colIdx = 2;
        for (DanhMucPhi fee : nhapTayFees) {
            Label lblFee = new Label(fee.getTenPhi());
            lblFee.setStyle(headerStyle);
            grid.add(lblFee, colIdx++, 0);
        }

        // Keep track of textfields for saving
        java.util.Map<Integer, java.util.Map<String, TextField>> inputsMap = new java.util.HashMap<>();
        java.util.Map<Integer, List<ChiTietHoaDon>> detailsMap = new java.util.HashMap<>();

        int rowIndex = 1;
        for (HoaDon hd : invoices) {
            Label lblHk = new Label(hd.getMaHoKhau());
            Label lblOwner = new Label(hd.getTenChuHo());
            lblHk.setStyle("-fx-font-weight: bold;");

            grid.add(lblHk, 0, rowIndex);
            grid.add(lblOwner, 1, rowIndex);

            List<ChiTietHoaDon> details = invoiceService.getInvoiceDetails(hd.getId());
            detailsMap.put(hd.getId(), details);

            java.util.Map<String, TextField> feeTextFields = new java.util.HashMap<>();

            int tempColIdx = 2;
            for (DanhMucPhi fee : nhapTayFees) {
                ChiTietHoaDon det = details.stream()
                        .filter(d -> fee.getMaPhi().equals(d.getMaPhi()))
                        .findFirst()
                        .orElse(null);

                TextField txtField = new TextField(det != null ? det.getSoLuong().toString() : "0");
                txtField.setPrefWidth(120);
                txtField.setDisable(!isDraft);
                setupNumericFormatter(txtField);
                grid.add(txtField, tempColIdx++, rowIndex);
                feeTextFields.put(fee.getMaPhi(), txtField);
            }

            inputsMap.put(hd.getId(), feeTextFields);
            rowIndex++;
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        dialog.getDialogPane().setContent(scrollPane);

        if (isDraft) {
            final Button btnSave = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            btnSave.addEventFilter(ActionEvent.ACTION, ae -> {
                try {
                    for (HoaDon hd : invoices) {
                        List<ChiTietHoaDon> details = detailsMap.get(hd.getId());
                        java.util.Map<String, TextField> feeTextFields = inputsMap.get(hd.getId());

                        for (DanhMucPhi fee : nhapTayFees) {
                            ChiTietHoaDon det = details.stream()
                                    .filter(d -> fee.getMaPhi().equals(d.getMaPhi()))
                                    .findFirst()
                                    .orElse(null);
                            if (det != null) {
                                String val = feeTextFields.get(fee.getMaPhi()).getText().trim();
                                det.setSoLuong(val.isEmpty() ? BigDecimal.ZERO : new BigDecimal(val));
                            }
                        }

                        invoiceService.saveInvoiceDetails(hd.getId(), details);
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu toàn bộ chỉ số cho các hộ gia đình thành công!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi lưu chỉ số: " + e.getMessage());
                    ae.consume();
                }
            });
        }

        dialog.showAndWait();
        loadBillingRuns();
    }

    private void setupNumericFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) return change;
            return null;
        }));
    }

    private void handlePublishRun(KhoanThu run) {
        // 1. Lấy danh sách hóa đơn hiện tại của đợt thu
        List<HoaDon> invoices = invoiceService.getInvoicesByRun(run.getId());
        if (invoices.isEmpty()) {
            // Không có hóa đơn nào, tiến hành tự động tạo
            try {
                com.bluemoon.repositories.HoaDonRepository hdRepo = new com.bluemoon.repositories.HoaDonRepository();
                boolean generated = hdRepo.createDraftInvoicesForRun(run.getId(), run.getNgayTao(), run.getHanNop());
                if (!generated) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tự động tạo hóa đơn cho đợt thu này.");
                    return;
                }
                invoices = invoiceService.getInvoicesByRun(run.getId());
                if (invoices.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy dữ liệu hộ dân nào để phát hành hóa đơn!");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi SQL", "Lỗi phát sinh khi tạo hóa đơn nháp: " + e.getMessage());
                return;
            }
        }

        // 2. Kiểm tra xem có hóa đơn nào có tổng tiền lớn hơn 0 hay chưa.
        // Đối với các phí tự nguyện hoặc đợt thu nói chung, nếu admin chưa nhập số liệu gì (tất cả có tổng tiền = 0)
        // thì hiển thị Alert báo lỗi và không cho phép phát hành.
        boolean hasData = false;
        for (HoaDon inv : invoices) {
            if (inv.getTongTien().compareTo(BigDecimal.ZERO) > 0) {
                hasData = true;
                break;
            }
        }

        if (!hasData) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đợt thu chưa có dữ liệu chỉ số hoặc đóng góp nào (Tất cả hóa đơn có số tiền = 0). Vui lòng nhập số liệu trước khi phát hành!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có muốn phát hành đợt thu '" + run.getTenKhoanThu() + "'?\nSau khi phát hành, hóa đơn sẽ được công bố chính thức cho cư dân và ghi nợ công nợ bắt buộc, không thể chỉnh sửa chỉ số nữa.", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận phát hành hóa đơn");
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            run.setTrangThai("PUBLISHED");
            try {
                if (billingRunService.updateFee(run)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đợt thu đã được phát hành chính thức!");
                    loadBillingRuns();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái đợt thu.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi SQL", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            }
        }
    }

    private void handleDeleteRun(KhoanThu run) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có muốn xóa đợt thu '" + run.getTenKhoanThu() + "'?\nTất cả hóa đơn và chi tiết liên quan sẽ bị xóa vĩnh viễn.", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận xóa đợt thu");
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (billingRunService.deleteFee(run.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa đợt thu.");
                loadBillingRuns();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa đợt thu.");
            }
        }
    }

    private TableCell<KhoanThu, Void> actionCellBillingRuns() {
        return new TableCell<>() {
            private final Button editBtn = createActionButton("Chi tiết");
            private final Button pubBtn = createActionButton("Phát hành");
            private final Button deleteBtn = createActionButton("🗑");
            private final HBox box = new HBox(6, editBtn, pubBtn, deleteBtn);
            {
                box.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> handleInputReadings(getTableView().getItems().get(getIndex())));
                pubBtn.setOnAction(e -> handlePublishRun(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteRun(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    KhoanThu kt = getTableView().getItems().get(getIndex());
                    if ("PUBLISHED".equals(kt.getTrangThai())) {
                        pubBtn.setDisable(true);
                        pubBtn.setText("Đã gửi");
                        editBtn.setText("Chi tiết");
                        deleteBtn.setDisable(true);
                    } else {
                        pubBtn.setDisable(false);
                        pubBtn.setText("Phát hành");
                        editBtn.setText("Nhập chỉ số");
                        deleteBtn.setDisable(false);
                    }
                    setGraphic(box);
                }
                setAlignment(Pos.CENTER);
            }
        };
    }

    private Button createActionButton(String content) {
        Button button = new Button(content);
        button.setStyle(
                "-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;");
        button.setOnMouseEntered(event -> button.setStyle(
                "-fx-background-color: #d9e7f5; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;"));
        button.setOnMouseExited(event -> button.setStyle(
                "-fx-background-color: #eef3f8; -fx-background-radius: 7; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;"));
        return button;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showDienPricingDialog(DanhMucPhi item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chỉnh sửa biểu giá điện bậc thang");
        dialog.setHeaderText("Cấu hình đơn giá và sản lượng tối đa cho từng bậc thang điện.");

        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        Label lblHeaderBac = new Label("Bậc thang");
        Label lblHeaderPrice = new Label("Đơn giá (đồng/kWh)");
        Label lblHeaderLimit = new Label("Sản lượng tối đa (kWh)");
        
        String headerStyle = "-fx-font-weight: bold; -fx-text-fill: #1e293b;";
        lblHeaderBac.setStyle(headerStyle);
        lblHeaderPrice.setStyle(headerStyle);
        lblHeaderLimit.setStyle(headerStyle);

        grid.add(lblHeaderBac, 0, 0);
        grid.add(lblHeaderPrice, 1, 0);
        grid.add(lblHeaderLimit, 2, 0);

        double[][] tiers = DanhMucPhi.parseDienTiers(item.getGhiChu());

        TextField[] txtPrices = new TextField[6];
        TextField[] txtLimits = new TextField[5];

        for (int i = 0; i < 6; i++) {
            grid.add(new Label("Bậc thang " + (i + 1)), 0, i + 1);

            txtPrices[i] = new TextField(String.valueOf((int) tiers[i][0]));
            txtPrices[i].setPrefWidth(150);
            setupIntegerFormatter(txtPrices[i]);
            grid.add(txtPrices[i], 1, i + 1);

            if (i < 5) {
                txtLimits[i] = new TextField(String.valueOf((int) tiers[i][1]));
                txtLimits[i].setPrefWidth(150);
                setupIntegerFormatter(txtLimits[i]);
                grid.add(txtLimits[i], 2, i + 1);
            } else {
                Label lblVoHan = new Label("Vô hạn");
                lblVoHan.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
                grid.add(lblVoHan, 2, i + 1);
            }
        }

        dialog.getDialogPane().setContent(grid);

        final Button btnOk = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        btnOk.addEventFilter(ActionEvent.ACTION, ae -> {
            for (int i = 0; i < 6; i++) {
                if (txtPrices[i].getText().trim().isEmpty() || (i < 5 && txtLimits[i].getText().trim().isEmpty())) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ giá trị cho tất cả các bậc!");
                    ae.consume();
                    return;
                }
            } 
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    double p = Double.parseDouble(txtPrices[i].getText().trim());
                    double l = (i < 5) ? Double.parseDouble(txtLimits[i].getText().trim()) : 0.0;
                    sb.append(p).append(",").append(l);
                    if (i < 5) {
                        sb.append(";");
                    }
                }
                item.setGhiChu(sb.toString());
                item.setDonGia(BigDecimal.ZERO);
                
                boolean success = fixedFeeService.updateDanhMucPhi(item);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu biểu giá điện bậc thang thành công.");
                    loadFixedFees();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu thông tin vào CSDL.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + e.getMessage());
            }
        }
    }

    private void setupIntegerFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) return change;
            return null;
        }));
    }
}
