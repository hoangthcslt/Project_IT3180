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

    public boolean isMaKhoanThuExists(String maKhoanThu) {
        return repository.isMaKhoanThuExists(maKhoanThu);
    }

    public boolean createFee(KhoanThu khoanThu) {
        if (khoanThu.getMaKhoanThu() == null || khoanThu.getMaKhoanThu().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khoản thu không được để trống");
        }
        return repository.insert(khoanThu);
    }

    public boolean updateFee(KhoanThu khoanThu) {
        if (khoanThu.getMaKhoanThu() == null || khoanThu.getMaKhoanThu().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khoản thu không được để trống");
        }
        return repository.update(khoanThu);
    }

    public boolean deleteFee(int id) {
        return repository.delete(id);
    }

    public List<KhoanThu> searchFees(String ma, String ten, String loai, String donGiaStr,
            java.time.LocalDate ngayTao) {
        return repository.search(ma, ten, loai, donGiaStr, ngayTao);
    }

    public BigDecimal calculateDebt(int hoKhauId, int khoanThuId) {
        return repository.calculateDebt(hoKhauId, khoanThuId);
    }
}
