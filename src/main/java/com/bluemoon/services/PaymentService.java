package com.bluemoon.services;

import java.math.BigDecimal;

public class PaymentService {

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
}
