package com.bluemoon.services;

import com.bluemoon.models.KhoanThu;
import com.bluemoon.repositories.FeeRepository;

import java.math.BigDecimal;
import java.util.List;

public class FeeService {
    private final FeeRepository repository;

    public FeeService() {
        this.repository = new FeeRepository();
    }

    public List<KhoanThu> getAllFees() {
        return repository.findAll();
    }

    public boolean createFee(KhoanThu khoanThu) {
        if (khoanThu.getMaKhoanThu() == null || khoanThu.getMaKhoanThu().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khoản thu không được để trống");
        }
        return repository.insert(khoanThu);
    }
    
    public BigDecimal calculateDebt(int hoKhauId, int khoanThuId) {
        return repository.calculateDebt(hoKhauId, khoanThuId);
    }
}
