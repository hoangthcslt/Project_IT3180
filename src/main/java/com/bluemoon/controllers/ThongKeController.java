package com.bluemoon.controllers;

import com.bluemoon.repositories.ThongKeRepository;
import com.bluemoon.utils.ExcelExporter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ThongKeController {

    // KPI Cards Bindings
    @FXML private Label lblTongCuDan;
    @FXML private Label lblTongCanHo;
    @FXML private Label lblTongDoanhThu;
    @FXML private Label lblTongNo;

    // Charts Bindings
    @FXML private PieChart chartGioiTinh;
    @FXML private BarChart<String, Number> chartTrangThai;
    @FXML private StackedBarChart<String, Number> chartNoVaThu;
    @FXML private AreaChart<String, Number> chartDoanhThu;

    // Filter Bindings
    @FXML private ComboBox<String> comboKieuThoiGian;
    @FXML private ComboBox<String> comboNamLoc;

    private ThongKeRepository thongKeRepository = new ThongKeRepository();
    private ExcelExporter excelExporter = new ExcelExporter();

    @FXML
    public void initialize() {
        loadKPIData();
        loadDemographicsData();
        loadFeeDebtData();
        initializeTimeFilters();
    }

    private void loadKPIData() {
        try {
            Map<String, Object> kpi = thongKeRepository.layThongKeTongQuan();
            
            Integer tongCuDan = (Integer) kpi.get("tongCuDan");
            Integer tongCanHo = (Integer) kpi.get("tongCanHo");
            BigDecimal tongDoanhThu = (BigDecimal) kpi.get("tongDoanhThu");
            BigDecimal tongNo = (BigDecimal) kpi.get("tongNo");

            lblTongCuDan.setText(tongCuDan != null ? String.format("%,d người", tongCuDan) : "0 người");
            lblTongCanHo.setText(tongCanHo != null ? String.format("%,d hộ", tongCanHo) : "0 hộ");
            lblTongDoanhThu.setText(formatCurrency(tongDoanhThu));
            lblTongNo.setText(formatCurrency(tongNo));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể tải thông tin KPI: " + e.getMessage());
        }
    }

    private void loadDemographicsData() {
        try {
            // Gender Stats (PieChart)
            List<Map<String, Object>> genderList = thongKeRepository.layThongKeGioiTinh();
            chartGioiTinh.getData().clear();
            for (Map<String, Object> row : genderList) {
                String rawGen = (String) row.get("gioiTinh");
                Integer count = (Integer) row.get("count");
                if (count == null) count = 0;
                chartGioiTinh.getData().add(new PieChart.Data(mapGender(rawGen) + " (" + count + ")", count.doubleValue()));
            }

            // Residency Status Stats (BarChart)
            List<Map<String, Object>> statusList = thongKeRepository.layThongKeTrangThai();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Số lượng");
            for (Map<String, Object> row : statusList) {
                String rawStatus = (String) row.get("trangThai");
                Integer count = (Integer) row.get("count");
                if (count == null) count = 0;
                series.getData().add(new XYChart.Data<>(mapStatus(rawStatus), count));
            }
            chartTrangThai.getData().clear();
            chartTrangThai.getData().add(series);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể tải thông tin dân cư: " + e.getMessage());
        }
    }

    private void loadFeeDebtData() {
        try {
            List<Map<String, Object>> feeList = thongKeRepository.layThongKeNoVaThu();
            
            XYChart.Series<String, Number> seriesDaNop = new XYChart.Series<>();
            seriesDaNop.setName("Đã nộp");

            XYChart.Series<String, Number> seriesChuaNop = new XYChart.Series<>();
            seriesChuaNop.setName("Chưa nộp");

            for (Map<String, Object> row : feeList) {
                String tenKT = (String) row.get("tenKhoanThu");
                BigDecimal daNop = (BigDecimal) row.get("tongDaNop");
                BigDecimal chuaNop = (BigDecimal) row.get("tongChuaNop");

                if (daNop == null) daNop = BigDecimal.ZERO;
                if (chuaNop == null) chuaNop = BigDecimal.ZERO;

                seriesDaNop.getData().add(new XYChart.Data<>(tenKT, daNop));
                seriesChuaNop.getData().add(new XYChart.Data<>(tenKT, chuaNop));
            }

            chartNoVaThu.getData().clear();
            chartNoVaThu.getData().addAll(seriesDaNop, seriesChuaNop);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể tải thông tin công nợ: " + e.getMessage());
        }
    }

    private void initializeTimeFilters() {
        // Setup options
        comboKieuThoiGian.getItems().addAll("Theo năm", "Theo tháng", "Theo tuần");
        comboKieuThoiGian.setValue("Theo tháng"); // Default

        comboNamLoc.getItems().add("Tất cả");
        try {
            List<Integer> years = thongKeRepository.layDanhSachNamCoGiaoDich();
            for (Integer yr : years) {
                comboNamLoc.getItems().add(String.valueOf(yr));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        comboNamLoc.setValue("Tất cả"); // Default

        // Set listeners
        comboKieuThoiGian.valueProperty().addListener((obs, oldVal, newVal) -> updateChartDoanhThu());
        comboNamLoc.valueProperty().addListener((obs, oldVal, newVal) -> updateChartDoanhThu());

        // Draw initial chart
        updateChartDoanhThu();
    }

    private void updateChartDoanhThu() {
        try {
            String kieu = "MONTH";
            String kieuSel = comboKieuThoiGian.getValue();
            if ("Theo năm".equals(kieuSel)) kieu = "YEAR";
            else if ("Theo tháng".equals(kieuSel)) kieu = "MONTH";
            else if ("Theo tuần".equals(kieuSel)) kieu = "WEEK";

            Integer nam = null;
            String namSel = comboNamLoc.getValue();
            if (namSel != null && !namSel.equals("Tất cả")) {
                nam = Integer.parseInt(namSel);
            }

            List<Map<String, Object>> revenueList = thongKeRepository.layDoanhThuTheoThoiGian(kieu, nam);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            for (Map<String, Object> row : revenueList) {
                String label = (String) row.get("label");
                BigDecimal val = (BigDecimal) row.get("val");
                if (val == null) val = BigDecimal.ZERO;
                series.getData().add(new XYChart.Data<>(label, val));
            }

            chartDoanhThu.getData().clear();
            chartDoanhThu.getData().add(series);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể cập nhật biểu đồ doanh thu: " + e.getMessage());
        }
    }

    @FXML
    public void handleXuatExcel(ActionEvent event) {
        try {
            Map<String, Object> kpiData = thongKeRepository.layThongKeTongQuan();
            List<Map<String, Object>> genderData = thongKeRepository.layThongKeGioiTinh();
            List<Map<String, Object>> statusData = thongKeRepository.layThongKeTrangThai();
            List<Map<String, Object>> feeData = thongKeRepository.layThongKeNoVaThu();
            
            String kieu = "MONTH";
            String kieuSel = comboKieuThoiGian.getValue();
            if ("Theo năm".equals(kieuSel)) kieu = "YEAR";
            else if ("Theo tháng".equals(kieuSel)) kieu = "MONTH";
            else if ("Theo tuần".equals(kieuSel)) kieu = "WEEK";

            Integer nam = null;
            String namSel = comboNamLoc.getValue();
            if (namSel != null && !namSel.equals("Tất cả")) {
                nam = Integer.parseInt(namSel);
            }
            List<Map<String, Object>> revenueData = thongKeRepository.layDoanhThuTheoThoiGian(kieu, nam);

            boolean success = excelExporter.exportThongKeToExcel(
                    kpiData,
                    genderData,
                    statusData,
                    feeData,
                    revenueData,
                    "BaoCaoThongKe.xlsx"
            );

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất báo cáo Excel thành công!\nFile đã được lưu tại thư mục gốc dự án: BaoCaoThongKe.xlsx");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất file Excel.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi truy vấn dữ liệu xuất Excel: " + e.getMessage());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        return String.format("%,.0f VND", amount.doubleValue());
    }

    private String mapGender(String dbGender) {
        if (dbGender == null) return "Khác";
        switch (dbGender.toUpperCase()) {
            case "NAM": return "Nam";
            case "NU": return "Nữ";
            default: return "Khác";
        }
    }

    private String mapStatus(String dbStatus) {
        if (dbStatus == null) return "Thường trú";
        switch (dbStatus.toUpperCase()) {
            case "THUONG_TRU": return "Thường trú";
            case "TAM_TRU": return "Tạm trú";
            case "TAM_VANG": return "Tạm vắng";
            default: return dbStatus;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
