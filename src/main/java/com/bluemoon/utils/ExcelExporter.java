package com.bluemoon.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public boolean exportThongKeToExcel(List<Map<String, Object>> data, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thong Ke Thu Phi");
            
            // Header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("STT");
            headerRow.createCell(1).setCellValue("Khoản Thu");
            headerRow.createCell(2).setCellValue("Tổng Đã Thu (VND)");
            
            // Data
            int rowIndex = 1;
            for (Map<String, Object> rowMap : data) {
                Row row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue(rowIndex);
                row.createCell(1).setCellValue((String) rowMap.get("tenKhoanThu"));
                
                BigDecimal daThu = (BigDecimal) rowMap.get("tongDaThu");
                row.createCell(2).setCellValue(daThu != null ? daThu.doubleValue() : 0.0);
                
                rowIndex++;
            }
            
            // Auto size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            
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
