package com.bluemoon.services;

import com.bluemoon.models.PaymentStatusView;
import com.bluemoon.repositories.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;

public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService() {
        this.repository = new PaymentRepository();
    }

    public BigDecimal validateTienNop(String soTienStr, BigDecimal soNoConLai) {
        try {
            BigDecimal soTien = new BigDecimal(soTienStr);
            
            if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số tiền nộp phải lớn hơn 0");
            }
            
            if (soTien.compareTo(soNoConLai) > 0) {
                throw new IllegalArgumentException("Số tiền nộp không được vượt quá số nợ còn lại");
            }
            
            return soTien;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vui lòng nhập số hợp lệ, không chứa ký tự chữ");
        }
    }

    public int countPaidItems() {
        return repository.countPaidItems();
    }

    public int countUnpaidItems() {
        return repository.countUnpaidItems();
    }

    public List<PaymentStatusView> findPaidItems(String keyword) {
        return repository.findPaidItems(keyword);
    }

    public List<PaymentStatusView> findUnpaidItems(String keyword) {
        return repository.findUnpaidItems(keyword);
    }

    public List<java.util.Map<String, Object>> findGiaoDichHistory(String keyword) {
        return repository.findGiaoDichHistory(keyword);
    }

    public boolean thucHienThanhToanHoaDon(int hoaDonId, BigDecimal soTien, String hinhThuc, String nguoiNop) throws java.sql.SQLException {
        return repository.thucHienThanhToanHoaDon(hoaDonId, soTien, hinhThuc, nguoiNop);
    }
}
