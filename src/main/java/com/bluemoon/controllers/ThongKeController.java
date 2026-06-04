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
    @FXML private PieChart chartTyLeCanHo;
    @FXML private LineChart<String, Number> chartDanCuTheoThoiGian;
    @FXML private BarChart<String, Number> chartDoanhThuTheoThang;
    @FXML private BarChart<String, Number> chartNoTheoThang;
    @FXML private StackedBarChart<String, Number> chartNoVaThu;

    private ThongKeRepository thongKeRepository = new ThongKeRepository();
    private ExcelExporter excelExporter = new ExcelExporter();

    @FXML
    public void initialize() {
        loadKPIData();
        loadDemographicsData();
        loadApartmentData();
        loadPopulationTrend();
        loadFeeDebtData();
        loadMonthlyFinancialCharts();
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
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể tải thông tin dân cư: " + e.getMessage());
        }
    }

    private void loadApartmentData() {
        try {
            List<Map<String, Object>> list = thongKeRepository.layThongKeTyLeCanHo();
            chartTyLeCanHo.getData().clear();
            for (Map<String, Object> row : list) {
                String status = (String) row.get("status");
                Integer count = (Integer) row.get("count");
                if (count == null) count = 0;
                if (status == null) status = "Trống";
                chartTyLeCanHo.getData().add(new PieChart.Data(status + " (" + count + ")", count.doubleValue()));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể tải thông tin trạng thái căn hộ: " + e.getMessage());
        }
    }

    private void loadPopulationTrend() {
        try {
            List<Map<String, Object>> list = thongKeRepository.layDanCuTheoThoiGian();
            chartDanCuTheoThoiGian.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Dân cư lũy tiến");

            int runningSum = 0;
            for (Map<String, Object> row : list) {
                java.time.LocalDate ngayLap = (java.time.LocalDate) row.get("ngayLap");
                Integer count = (Integer) row.get("count");
                if (count == null) count = 0;
                runningSum += count;
                
                String dateStr = ngayLap != null ? ngayLap.toString() : "N/A";
                series.getData().add(new XYChart.Data<>(dateStr, runningSum));
            }
            chartDanCuTheoThoiGian.getData().add(series);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể tải xu hướng dân cư: " + e.getMessage());
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

    private void loadMonthlyFinancialCharts() {
        try {
            // Doanh thu theo tháng
            List<Map<String, Object>> revenueList = thongKeRepository.layDoanhThuTheoThang();
            chartDoanhThuTheoThang.getData().clear();
            XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
            revSeries.setName("Doanh thu");
            for (Map<String, Object> row : revenueList) {
                String label = (String) row.get("label");
                BigDecimal val = (BigDecimal) row.get("val");
                if (val == null) val = BigDecimal.ZERO;
                revSeries.getData().add(new XYChart.Data<>(label, val));
            }
            chartDoanhThuTheoThang.getData().add(revSeries);

            // Nợ theo tháng
            List<Map<String, Object>> debtList = thongKeRepository.layNoTheoThang();
            chartNoTheoThang.getData().clear();
            XYChart.Series<String, Number> debtSeries = new XYChart.Series<>();
            debtSeries.setName("Tiền nợ");
            for (Map<String, Object> row : debtList) {
                String label = (String) row.get("label");
                BigDecimal val = (BigDecimal) row.get("val");
                if (val == null) val = BigDecimal.ZERO;
                debtSeries.getData().add(new XYChart.Data<>(label, val));
            }
            chartNoTheoThang.getData().add(debtSeries);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể tải biểu đồ tài chính: " + e.getMessage());
        }
    }

    @FXML
    public void handleXuatExcel(ActionEvent event) {
        try {
            Map<String, Object> kpiData = thongKeRepository.layThongKeTongQuan();
            List<Map<String, Object>> genderData = thongKeRepository.layThongKeGioiTinh();
            List<Map<String, Object>> statusData = thongKeRepository.layThongKeTrangThai();
            List<Map<String, Object>> feeData = thongKeRepository.layThongKeNoVaThu();
            List<Map<String, Object>> revenueData = thongKeRepository.layDoanhThuTheoThoiGian("MONTH", null);

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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
