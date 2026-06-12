package com.bluemoon.controllers;

import com.bluemoon.models.HoaDon;
import com.bluemoon.models.ChiTietHoaDon;
import com.bluemoon.models.DanhMucPhi;
import com.bluemoon.models.PaymentStatusView;
import com.bluemoon.services.HoaDonService;
import com.bluemoon.services.PaymentService;
import com.bluemoon.services.DanhMucPhiService;
import com.bluemoon.repositories.PaymentRepository;

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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaymentController {

    // Tab 1: Invoices
    @FXML private TableView<HoaDon> tableInvoices;
    @FXML private TableColumn<HoaDon, Integer> colInvId;
    @FXML private TableColumn<HoaDon, String> colInvCode;
    @FXML private TableColumn<HoaDon, String> colInvHk;
    @FXML private TableColumn<HoaDon, String> colInvOwner;
    @FXML private TableColumn<HoaDon, String> colInvPeriod;
    @FXML private TableColumn<HoaDon, BigDecimal> colInvTotal;
    @FXML private TableColumn<HoaDon, BigDecimal> colInvPaid;
    @FXML private TableColumn<HoaDon, String> colInvStatus;
    @FXML private TableColumn<HoaDon, Void> colInvActions;
    @FXML private TextField txtSearchInvoice;
    @FXML private ComboBox<String> cbFilterStatus;

    // Tab 2: Transaction History
    @FXML private TableView<Map<String, Object>> tableTransactions;
    @FXML private TableColumn<Map<String, Object>, Integer> colTxId;
    @FXML private TableColumn<Map<String, Object>, String> colTxInvCode;
    @FXML private TableColumn<Map<String, Object>, String> colTxHk;
    @FXML private TableColumn<Map<String, Object>, String> colTxOwner;
    @FXML private TableColumn<Map<String, Object>, String> colTxPayer;
    @FXML private TableColumn<Map<String, Object>, BigDecimal> colTxAmount;
    @FXML private TableColumn<Map<String, Object>, LocalDate> colTxDate;
    @FXML private TableColumn<Map<String, Object>, String> colTxMethod;
    @FXML private TextField txtSearchTx;

    private HoaDonService invoiceService;
    private PaymentService paymentService;

    private ObservableList<HoaDon> invoiceList;
    private ObservableList<Map<String, Object>> transactionList;

    @FXML
    public void initialize() {
        invoiceService = new HoaDonService();
        paymentService = new PaymentService();

        invoiceList = FXCollections.observableArrayList();
        transactionList = FXCollections.observableArrayList();

        // 1. Setup Invoices Table Columns
        colInvId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colInvCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaHoaDon()));
        colInvHk.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaHoKhau()));
        colInvOwner.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChuHo()));
        colInvPeriod.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaHoKhau())); // Temp, will map run name below
        colInvTotal.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTongTien()));
        colInvPaid.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSoTienDaNop()));
        colInvStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colInvActions.setCellValueFactory(cell -> new SimpleObjectProperty<>(null));
        colInvActions.setCellFactory(col -> actionCellInvoices());

        // Dynamic mapper for billing run name
        colInvPeriod.setCellValueFactory(cell -> {
            int runId = cell.getValue().getKhoanThuId();
            // Fetch run details
            String runName = "Đợt thu #" + runId;
            try {
                com.bluemoon.models.KhoanThu run = new com.bluemoon.repositories.FeeRepository().findAll().stream()
                        .filter(r -> r.getId() == runId)
                        .findFirst()
                        .orElse(null);
                if (run != null) {
                    runName = run.getTenKhoanThu();
                }
            } catch (Exception e) {}
            return new SimpleStringProperty(runName);
        });

        // 2. Setup Transactions Table Columns
        colTxId.setCellValueFactory(cell -> new SimpleObjectProperty<>((Integer) cell.getValue().get("id")));
        colTxInvCode.setCellValueFactory(cell -> new SimpleStringProperty((String) cell.getValue().get("maHoaDon")));
        colTxHk.setCellValueFactory(cell -> new SimpleStringProperty((String) cell.getValue().get("maHoKhau")));
        colTxOwner.setCellValueFactory(cell -> new SimpleStringProperty((String) cell.getValue().get("tenChuHo")));
        colTxPayer.setCellValueFactory(cell -> new SimpleStringProperty((String) cell.getValue().get("nguoiNop")));
        colTxAmount.setCellValueFactory(cell -> new SimpleObjectProperty<>((BigDecimal) cell.getValue().get("soTienNop")));
        colTxDate.setCellValueFactory(cell -> new SimpleObjectProperty<>((LocalDate) cell.getValue().get("ngayNop")));
        colTxMethod.setCellValueFactory(cell -> new SimpleStringProperty((String) cell.getValue().get("hinhThuc")));

        tableInvoices.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTransactions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        cbFilterStatus.setValue("TẤT CẢ");

        loadInvoices();
        loadTransactions();
    }

    private void loadInvoices() {
        String filter = cbFilterStatus.getValue();
        if ("TẤT CẢ".equals(filter)) {
            filter = null;
        }
        invoiceList.setAll(invoiceService.searchInvoices(null, filter, null));
        tableInvoices.setItems(invoiceList);
    }

    private void loadTransactions() {
        transactionList.setAll(paymentService.findGiaoDichHistory(null));
        tableTransactions.setItems(transactionList);
    }

    // --- TAB 1 ACTIONS ---

    @FXML
    void handleSearchInvoice(ActionEvent event) {
        String kw = txtSearchInvoice.getText();
        String filter = cbFilterStatus.getValue();
        if ("TẤT CẢ".equals(filter)) {
            filter = null;
        }
        invoiceList.setAll(invoiceService.searchInvoices(kw, filter, null));
    }

    @FXML
    void handleResetInvoice(ActionEvent event) {
        txtSearchInvoice.clear();
        cbFilterStatus.setValue("TẤT CẢ");
        loadInvoices();
    }

    private void handlePayInvoice(HoaDon inv) {
        DanhMucPhiService feeService = new DanhMucPhiService();
        List<DanhMucPhi> allFees = feeService.getAllDanhMucPhi();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Hóa đơn: " + inv.getMaHoaDon());
        dialog.setHeaderText("Chi tiết các phí căn hộ: " + inv.getMaHoKhau() + " - " + inv.getTenChuHo());
        dialog.getDialogPane().setMinWidth(600);

        ButtonType closeButtonType = new ButtonType("Đóng", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        List<ChiTietHoaDon> details = invoiceService.getInvoiceDetails(inv.getId());

        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        // Details table view
        TableView<ChiTietHoaDon> tableDetails = new TableView<>();
        tableDetails.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableDetails.setPrefHeight(200);

        TableColumn<ChiTietHoaDon, String> colName = new TableColumn<>("Khoản Thu");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenPhi()));
        
        TableColumn<ChiTietHoaDon, BigDecimal> colPrice = new TableColumn<>("Đơn Giá");
        colPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDonGia()));
        
        TableColumn<ChiTietHoaDon, BigDecimal> colQty = new TableColumn<>("Số Lượng");
        colQty.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSoLuong()));
        
        TableColumn<ChiTietHoaDon, BigDecimal> colTotal = new TableColumn<>("Thành Tiền");
        colTotal.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getThanhTien()));

        tableDetails.getColumns().addAll(colName, colPrice, colQty, colTotal);
        tableDetails.getItems().setAll(details);

        box.getChildren().addAll(new Label("Danh mục phí chi tiết:"), tableDetails);

        final Label[] lblRemainingRef = new Label[1];
        final TextField[] txtAmountRef = new TextField[1];
        final BigDecimal[] currentTotalRef = new BigDecimal[1];
        currentTotalRef[0] = inv.getTongTien();

        Runnable recalculateTotals = () -> {
            BigDecimal newTotal = BigDecimal.ZERO;
            for (ChiTietHoaDon det : tableDetails.getItems()) {
                newTotal = newTotal.add(det.getThanhTien());
            }
            currentTotalRef[0] = newTotal;
            BigDecimal rem = newTotal.subtract(inv.getSoTienDaNop());
            if (lblRemainingRef[0] != null) {
                lblRemainingRef[0].setText(rem.toString() + " đ");
            }
            if (txtAmountRef[0] != null) {
                txtAmountRef[0].setText(rem.toString());
            }
        };

        colPrice.setCellFactory(column -> new TableCell<ChiTietHoaDon, BigDecimal>() {
            private final TextField textField = new TextField();
            {
                textField.setPrefWidth(100);
                textField.setTextFormatter(new TextFormatter<>(change -> {
                    if (change.getControlNewText().matches("\\d*\\.?\\d*")) return change;
                    return null;
                }));
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        commitChange();
                    }
                });
                textField.setOnAction(e -> commitChange());
            }

            private void commitChange() {
                ChiTietHoaDon rowItem = getTableRow().getItem();
                if (rowItem != null) {
                    try {
                        String txtVal = textField.getText().trim();
                        BigDecimal newPrice = txtVal.isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtVal);
                        rowItem.setDonGia(newPrice);
                        rowItem.setThanhTien(newPrice.multiply(rowItem.getSoLuong()));
                        getTableView().refresh();
                        recalculateTotals.run();
                    } catch (NumberFormatException e) {
                        textField.setText(rowItem.getDonGia().toString());
                    }
                }
            }

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    ChiTietHoaDon rowItem = getTableRow().getItem();
                    boolean isVoluntary = allFees.stream()
                            .anyMatch(f -> rowItem.getMaPhi().equals(f.getMaPhi()) && "TU_NGUYEN".equals(f.getLoaiPhi()));
                    boolean canEdit = "CHUA_NOP".equals(inv.getTrangThai()) && isVoluntary;
                    if (canEdit) {
                        textField.setText(item != null ? item.toString() : "0");
                        setGraphic(textField);
                        setText(null);
                    } else {
                        setGraphic(null);
                        if ("DIEN".equals(rowItem.getMaPhi())) {
                            setText("Bậc thang");
                        } else {
                            setText(item != null ? item.toString() : "0");
                        }
                    }
                }
            }
        });

        BigDecimal remaining = inv.getTongTien().subtract(inv.getSoTienDaNop());

        // Payment form section if not paid
        if ("CHUA_NOP".equals(inv.getTrangThai()) && remaining.compareTo(BigDecimal.ZERO) > 0) {
            Label separator = new Label("--------------------------------------------------------------------------------");
            separator.setStyle("-fx-text-fill: #cbd5e1;");

            GridPane payGrid = new GridPane();
            payGrid.setHgap(10);
            payGrid.setVgap(10);

            lblRemainingRef[0] = new Label(remaining.toString() + " đ");
            lblRemainingRef[0].setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");

            ComboBox<String> cbMethod = new ComboBox<>(FXCollections.observableArrayList("TIEN_MAT", "CHUYEN_KHOAN"));
            cbMethod.setValue("TIEN_MAT");

            TextField txtPayer = new TextField(inv.getTenChuHo());
            txtAmountRef[0] = new TextField(remaining.toString());

            txtAmountRef[0].setTextFormatter(new TextFormatter<>(change -> {
                if (change.getControlNewText().matches("\\d*\\.?\\d*")) return change;
                return null;
            }));

            Button payBtn = new Button("Xác nhận thanh toán");
            payBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");

            payGrid.add(new Label("Nợ còn lại:"), 0, 0);
            payGrid.add(lblRemainingRef[0], 1, 0);
            payGrid.add(new Label("Hình thức:"), 0, 1);
            payGrid.add(cbMethod, 1, 1);
            payGrid.add(new Label("Người nộp:"), 0, 2);
            payGrid.add(txtPayer, 1, 2);
            payGrid.add(new Label("Số tiền đóng:"), 0, 3);
            payGrid.add(txtAmountRef[0], 1, 3);
            payGrid.add(payBtn, 1, 4);

            payBtn.setOnAction(e -> {
                if (txtPayer.getText().trim().isEmpty() || txtAmountRef[0].getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập người nộp và số tiền!");
                    return;
                }
                try {
                    // 1. Save updated details to DB (which updates tong_tien on the invoice)
                    boolean detailsSaved = invoiceService.saveInvoiceDetails(inv.getId(), details);
                    if (!detailsSaved) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu chi tiết hóa đơn.");
                        return;
                    }

                    BigDecimal amt = new BigDecimal(txtAmountRef[0].getText().trim());
                    if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền đóng phải lớn hơn 0!");
                        return;
                    }
                    BigDecimal newRemaining = currentTotalRef[0].subtract(inv.getSoTienDaNop());
                    if (amt.compareTo(newRemaining) > 0) {
                        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền đóng không được vượt quá số nợ còn lại (" + newRemaining + " đ)!");
                        return;
                    }

                    boolean ok = paymentService.thucHienThanhToanHoaDon(inv.getId(), amt, cbMethod.getValue(), txtPayer.getText().trim());
                    if (ok) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Ghi nhận thanh toán thành công!");
                        dialog.close();
                        loadInvoices();
                        loadTransactions();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể ghi nhận giao dịch.");
                    }
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi: " + ex.getMessage());
                }
            });

            box.getChildren().addAll(separator, new Label("Ghi nhận thanh toán:"), payGrid);
        } else {
            Label lblSuccess = new Label("Hóa đơn này đã được thanh toán đầy đủ.");
            lblSuccess.setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e; -fx-font-size: 14px;");
            box.getChildren().add(lblSuccess);
        }

        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private TableCell<HoaDon, Void> actionCellInvoices() {
        return new TableCell<>() {
            private final Button payBtn = new Button("Xem & Thu tiền");
            {
                payBtn.setStyle("-fx-background-color: #eef3f8; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
                payBtn.setOnAction(e -> handlePayInvoice(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HoaDon hd = getTableView().getItems().get(getIndex());
                    if ("DA_NOP".equals(hd.getTrangThai())) {
                        payBtn.setText("Xem chi tiết");
                        payBtn.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6; -fx-text-fill: #64748b; -fx-cursor: hand;");
                    } else {
                        payBtn.setText("Thu tiền");
                        payBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
                    }
                    setGraphic(payBtn);
                }
                setAlignment(Pos.CENTER);
            }
        };
    }

    // --- TAB 2 ACTIONS ---

    @FXML
    void handleSearchTx(ActionEvent event) {
        String kw = txtSearchTx.getText();
        if (kw == null || kw.trim().isEmpty()) {
            loadTransactions();
            return;
        }
        transactionList.setAll(paymentService.findGiaoDichHistory(kw));
    }

    @FXML
    void handleResetTx(ActionEvent event) {
        txtSearchTx.clear();
        loadTransactions();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
