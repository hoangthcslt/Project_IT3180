package com.bluemoon.controllers;

import com.bluemoon.repositories.ThongKeRepository;
import com.bluemoon.utils.ExcelExporter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ThongKeController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    private ThongKeRepository thongKeRepository = new ThongKeRepository();
    private ExcelExporter excelExporter = new ExcelExporter();
    
    private List<Map<String, Object>> duLieuThongKe;

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            duLieuThongKe = thongKeRepository.layDuLieuThongKe();
            
            XYChart.Series<String, Number> seriesDaThu = new XYChart.Series<>();
            seriesDaThu.setName("Đã Thu");

            for (Map<String, Object> row : duLieuThongKe) {
                String tenKT = (String) row.get("tenKhoanThu");
                BigDecimal daThu = (BigDecimal) row.get("tongDaThu");
                if (daThu == null) daThu = BigDecimal.ZERO;
                
                seriesDaThu.getData().add(new XYChart.Data<>(tenKT, daThu));
                pieChart.getData().add(new PieChart.Data(tenKT, daThu.doubleValue()));
            }
            
            barChart.getData().add(seriesDaThu);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", e.getMessage());
        }
    }

    @FXML
    public void handleXuatExcel(ActionEvent event) {
        if (duLieuThongKe != null && !duLieuThongKe.isEmpty()) {
            boolean success = excelExporter.exportThongKeToExcel(duLieuThongKe, "BaoCaoThongKe.xlsx");
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất file Excel thành công!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất file Excel.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dữ liệu để xuất.");
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
