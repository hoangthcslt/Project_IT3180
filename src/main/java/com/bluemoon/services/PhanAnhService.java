package com.bluemoon.services;

import com.bluemoon.models.PhanAnh;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PhanAnhService {
    private static final List<PhanAnh> reportStore = new ArrayList<>();
    private static int nextId = 1;

    public List<PhanAnh> getAllReports() {
        return new ArrayList<>(reportStore);
    }

    public boolean addReport(PhanAnh phanAnh) {
        if (phanAnh == null) {
            return false;
        }
        phanAnh.setId(nextId++);
        if (phanAnh.getNgayGui() == null) {
            phanAnh.setNgayGui(LocalDate.now());
        }
        if (phanAnh.getTrangThai() == null || phanAnh.getTrangThai().isBlank()) {
            phanAnh.setTrangThai("Chờ đợi");
        }
        if (phanAnh.getNguoiGui() == null || phanAnh.getNguoiGui().isBlank()) {
            phanAnh.setNguoiGui("Cư dân");
        }
        reportStore.add(phanAnh);
        return true;
    }

    public boolean updateReport(PhanAnh phanAnh) {
        if (phanAnh == null) {
            return false;
        }
        for (int i = 0; i < reportStore.size(); i++) {
            if (reportStore.get(i).getId() == phanAnh.getId()) {
                reportStore.set(i, phanAnh);
                return true;
            }
        }
        return false;
    }

    public boolean deleteReport(int id) {
        return reportStore.removeIf(report -> report.getId() == id);
    }

    public List<PhanAnh> searchReports(String keyword, String status) {
        String normalizedKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase(Locale.forLanguageTag("vi"));
        String normalizedStatus = (status == null || status.isBlank() || "Tất cả".equals(status)) ? null
                : status.trim();

        return reportStore.stream()
                .filter(report -> {
                    boolean matchesKeyword = normalizedKeyword.isEmpty()
                            || report.getTieuDe().toLowerCase(Locale.forLanguageTag("vi")).contains(normalizedKeyword)
                            || report.getNoiDung().toLowerCase(Locale.forLanguageTag("vi")).contains(normalizedKeyword);
                    boolean matchesStatus = normalizedStatus == null || normalizedStatus.equals(report.getTrangThai());
                    return matchesKeyword && matchesStatus;
                })
                .collect(Collectors.toList());
    }

    public List<String> getAvailableCategories() {
        return Collections.unmodifiableList(Arrays.asList("Quản lí", "Môi trường", "An ninh", "Kinh tế", "Dịch vụ", "Cơ sở vật chất", "Kiến nghị riêng"));
    }

    public List<String> getAvailableAssignees() {
        return Collections.unmodifiableList(Arrays.asList("Ban quản lý", "Kế toán", "Bảo vệ", "Kỹ thuật"));
    }
}
