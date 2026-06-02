package com.bluemoon.utils;

import com.bluemoon.models.HoKhau;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.models.ThongBao;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public boolean exportNotificationsToExcel(List<ThongBao> notifications, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách thông báo");
            String[] headers = {"STT", "Tên thông báo", "File đính kèm", "Ngày ban hành", "Nhóm nhận", "Trạng thái"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }
            for (int i = 0; i < notifications.size(); i++) {
                ThongBao item = notifications.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(item.getTenThongBao());
                row.createCell(2).setCellValue(valueOrEmpty(item.getFilePath()));
                if (item.getNgayBanHanh() != null) {
                    row.createCell(3).setCellValue(java.sql.Date.valueOf(item.getNgayBanHanh()));
                    row.getCell(3).setCellStyle(dateStyle);
                }
                row.createCell(4).setCellValue(valueOrEmpty(item.getNhomNhan()));
                row.createCell(5).setCellValue(valueOrEmpty(item.getTrangThai()));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream output = new FileOutputStream(filePath)) { workbook.write(output); }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportResidentsToExcel(List<NhanKhau> residents, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách nhân khẩu");
            String[] headers = {"ID", "ID Hộ Khẩu", "Họ Tên", "CCCD", "SĐT", "Ngày Sinh", "Giới Tính",
                    "Quan Hệ", "Trạng Thái"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }
            for (int i = 0; i < residents.size(); i++) {
                NhanKhau resident = residents.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(resident.getId());
                row.createCell(1).setCellValue(resident.getHoKhauId());
                row.createCell(2).setCellValue(resident.getHoTen());
                row.createCell(3).setCellValue(valueOrEmpty(resident.getCccd()));
                row.createCell(4).setCellValue(valueOrEmpty(resident.getSoDienThoai()));
                if (resident.getNgaySinh() != null) {
                    row.createCell(5).setCellValue(java.sql.Date.valueOf(resident.getNgaySinh()));
                    row.getCell(5).setCellStyle(dateStyle);
                }
                row.createCell(6).setCellValue(valueOrEmpty(resident.getGioiTinh()));
                row.createCell(7).setCellValue(valueOrEmpty(resident.getQuanHe()));
                row.createCell(8).setCellValue(valueOrEmpty(resident.getTrangThai()));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream output = new FileOutputStream(filePath)) {
                workbook.write(output);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    public boolean exportHouseholdsToExcel(List<HoKhau> households, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách hộ khẩu");
            String[] headers = {"ID", "Mã hộ khẩu", "Tên chủ hộ", "Diện tích", "Trạng thái", "Số người",
                    "Phương tiện", "Ngày lập"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }

            for (int i = 0; i < households.size(); i++) {
                HoKhau household = households.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(household.getId());
                row.createCell(1).setCellValue(household.getMaHoKhau());
                row.createCell(2).setCellValue(household.getTenChuHo());
                row.createCell(3).setCellValue(household.getDienTich().doubleValue());
                row.createCell(4).setCellValue(household.getTrangThai());
                row.createCell(5).setCellValue(household.getSoNguoi());
                row.createCell(6).setCellValue(household.getPhuongTien());
                row.createCell(7).setCellValue(java.sql.Date.valueOf(household.getNgayLap()));
                row.getCell(7).setCellStyle(dateStyle);
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream output = new FileOutputStream(filePath)) {
                workbook.write(output);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportThongKeToExcel(List<Map<String, Object>> data, String filePath) {
        return exportThongKeToExcel(null, null, null, data, null, filePath);
    }

    public boolean exportThongKeToExcel(
            Map<String, Object> kpiData,
            List<Map<String, Object>> genderData,
            List<Map<String, Object>> statusData,
            List<Map<String, Object>> feeData,
            List<Map<String, Object>> revenueData,
            String filePath
    ) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Setup styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(cellStyle);
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));

            // SHEET 1: TỔNG QUAN & DÂN CƯ
            Sheet sheet1 = workbook.createSheet("Tổng quan & Dân cư");
            int r1 = 0;
            
            // KPI Data
            Row kpiTitleRow = sheet1.createRow(r1++);
            Cell titleCell = kpiTitleRow.createCell(0);
            titleCell.setCellValue("CHỈ SỐ TỔNG QUAN HỆ THỐNG");
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 12);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            r1++; // empty row
            
            if (kpiData != null) {
                Row hRow = sheet1.createRow(r1++);
                hRow.createCell(0).setCellValue("Chỉ số");
                hRow.createCell(1).setCellValue("Giá trị");
                hRow.getCell(0).setCellStyle(headerStyle);
                hRow.getCell(1).setCellStyle(headerStyle);

                String[] keys = {"tongCuDan", "tongCanHo", "tongDoanhThu", "tongNo"};
                String[] labels = {"Tổng số cư dân (người)", "Tổng số căn hộ", "Tổng doanh thu (VND)", "Tổng nợ chưa nộp (VND)"};
                for (int i = 0; i < keys.length; i++) {
                    Row row = sheet1.createRow(r1++);
                    row.createCell(0).setCellValue(labels[i]);
                    row.getCell(0).setCellStyle(cellStyle);
                    
                    Cell valCell = row.createCell(1);
                    valCell.setCellStyle(cellStyle);
                    Object val = kpiData.get(keys[i]);
                    if (val instanceof Number) {
                        valCell.setCellValue(((Number) val).doubleValue());
                        if (keys[i].equals("tongDoanhThu") || keys[i].equals("tongNo")) {
                            valCell.setCellStyle(currencyStyle);
                        }
                    } else {
                        valCell.setCellValue(val != null ? val.toString() : "0");
                    }
                }
            }
            
            r1 += 2; // empty rows
            
            // Gender stats
            if (genderData != null) {
                Row genTitleRow = sheet1.createRow(r1++);
                Cell c = genTitleRow.createCell(0);
                c.setCellValue("THỐNG KÊ CƯ DÂN THEO GIỚI TÍNH");
                CellStyle subTitleStyle = workbook.createCellStyle();
                Font subTitleFont = workbook.createFont();
                subTitleFont.setBold(true);
                subTitleStyle.setFont(subTitleFont);
                c.setCellStyle(subTitleStyle);
                
                Row hRow = sheet1.createRow(r1++);
                hRow.createCell(0).setCellValue("Giới tính");
                hRow.createCell(1).setCellValue("Số lượng");
                hRow.getCell(0).setCellStyle(headerStyle);
                hRow.getCell(1).setCellStyle(headerStyle);

                for (Map<String, Object> map : genderData) {
                    Row row = sheet1.createRow(r1++);
                    String gt = (String) map.get("gioiTinh");
                    Integer count = (Integer) map.get("count");
                    row.createCell(0).setCellValue(gt != null ? gt : "Khác");
                    row.createCell(1).setCellValue(count != null ? count : 0);
                    row.getCell(0).setCellStyle(cellStyle);
                    row.getCell(1).setCellStyle(cellStyle);
                }
            }
            
            r1 += 2;
            
            // Residency status stats
            if (statusData != null) {
                Row statTitleRow = sheet1.createRow(r1++);
                Cell c = statTitleRow.createCell(0);
                c.setCellValue("THỐNG KÊ TÌNH TRẠNG CƯ TRÚ");
                CellStyle subTitleStyle = workbook.createCellStyle();
                Font subTitleFont = workbook.createFont();
                subTitleFont.setBold(true);
                subTitleStyle.setFont(subTitleFont);
                c.setCellStyle(subTitleStyle);

                Row hRow = sheet1.createRow(r1++);
                hRow.createCell(0).setCellValue("Trạng thái cư trú");
                hRow.createCell(1).setCellValue("Số lượng");
                hRow.getCell(0).setCellStyle(headerStyle);
                hRow.getCell(1).setCellStyle(headerStyle);

                for (Map<String, Object> map : statusData) {
                    Row row = sheet1.createRow(r1++);
                    String tt = (String) map.get("trangThai");
                    Integer count = (Integer) map.get("count");
                    row.createCell(0).setCellValue(tt != null ? tt : "Thường trú");
                    row.createCell(1).setCellValue(count != null ? count : 0);
                    row.getCell(0).setCellStyle(cellStyle);
                    row.getCell(1).setCellStyle(cellStyle);
                }
            }
            
            sheet1.autoSizeColumn(0);
            sheet1.autoSizeColumn(1);

            // SHEET 2: CHI TIẾT CÁC KHOẢN THU
            Sheet sheet2 = workbook.createSheet("Chi tiết các khoản thu");
            Row headerRow2 = sheet2.createRow(0);
            headerRow2.createCell(0).setCellValue("STT");
            headerRow2.createCell(1).setCellValue("Khoản Thu");
            headerRow2.createCell(2).setCellValue("Tổng Đã Nộp (VND)");
            headerRow2.createCell(3).setCellValue("Tổng Chưa Nộp (VND)");
            
            for (int i = 0; i < 4; i++) {
                headerRow2.createCell(i); // ensure created
                headerRow2.getCell(i).setCellValue(i == 0 ? "STT" : i == 1 ? "Khoản Thu" : i == 2 ? "Tổng Đã Nộp (VND)" : "Tổng Chưa Nộp (VND)");
                headerRow2.getCell(i).setCellStyle(headerStyle);
            }
            
            int rowIndex2 = 1;
            if (feeData != null) {
                for (Map<String, Object> rowMap : feeData) {
                    Row row = sheet2.createRow(rowIndex2);
                    row.createCell(0).setCellValue(rowIndex2);
                    row.createCell(1).setCellValue((String) rowMap.get("tenKhoanThu"));
                    
                    Cell cellNop = row.createCell(2);
                    BigDecimal daNop = (BigDecimal) rowMap.get("tongDaNop");
                    if (daNop == null) daNop = (BigDecimal) rowMap.get("tongDaThu"); // fallback to layDuLieuThongKe format
                    cellNop.setCellValue(daNop != null ? daNop.doubleValue() : 0.0);
                    cellNop.setCellStyle(currencyStyle);
                    
                    Cell cellChuaNop = row.createCell(3);
                    BigDecimal chuaNop = (BigDecimal) rowMap.get("tongChuaNop");
                    cellChuaNop.setCellValue(chuaNop != null ? chuaNop.doubleValue() : 0.0);
                    cellChuaNop.setCellStyle(currencyStyle);
                    
                    row.getCell(0).setCellStyle(cellStyle);
                    row.getCell(1).setCellStyle(cellStyle);
                    
                    rowIndex2++;
                }
            }
            sheet2.autoSizeColumn(0);
            sheet2.autoSizeColumn(1);
            sheet2.autoSizeColumn(2);
            sheet2.autoSizeColumn(3);

            // SHEET 3: DOANH THU THEO THỜI GIAN
            Sheet sheet3 = workbook.createSheet("Doanh thu theo thời gian");
            Row headerRow3 = sheet3.createRow(0);
            headerRow3.createCell(0).setCellValue("STT");
            headerRow3.createCell(1).setCellValue("Mốc thời gian");
            headerRow3.createCell(2).setCellValue("Doanh thu (VND)");
            
            for (int i = 0; i < 3; i++) {
                headerRow3.createCell(i); // ensure created
                headerRow3.getCell(i).setCellValue(i == 0 ? "STT" : i == 1 ? "Mốc thời gian" : "Doanh thu (VND)");
                headerRow3.getCell(i).setCellStyle(headerStyle);
            }

            int rowIndex3 = 1;
            if (revenueData != null) {
                for (Map<String, Object> rowMap : revenueData) {
                    Row row = sheet3.createRow(rowIndex3);
                    row.createCell(0).setCellValue(rowIndex3);
                    row.createCell(1).setCellValue((String) rowMap.get("label"));
                    
                    Cell cellDoanhThu = row.createCell(2);
                    BigDecimal doanhThu = (BigDecimal) rowMap.get("val");
                    cellDoanhThu.setCellValue(doanhThu != null ? doanhThu.doubleValue() : 0.0);
                    cellDoanhThu.setCellStyle(currencyStyle);
                    
                    row.getCell(0).setCellStyle(cellStyle);
                    row.getCell(1).setCellStyle(cellStyle);
                    
                    rowIndex3++;
                }
            }
            sheet3.autoSizeColumn(0);
            sheet3.autoSizeColumn(1);
            sheet3.autoSizeColumn(2);

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
