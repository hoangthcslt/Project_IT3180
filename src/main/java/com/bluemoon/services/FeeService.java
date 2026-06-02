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
        if (khoanThu.getHanNop() == null) {
            throw new IllegalArgumentException("Hạn nộp không được để trống");
        }
        khoanThu.setTrangThai("DRAFT");
        boolean success = repository.insert(khoanThu);
        if (success) {
            try {
                com.bluemoon.repositories.HoaDonRepository hdRepo = new com.bluemoon.repositories.HoaDonRepository();
                boolean draftCreated = hdRepo.createDraftInvoicesForRun(khoanThu.getId(), khoanThu.getNgayTao(), khoanThu.getHanNop());
                if (!draftCreated) {
                    repository.delete(khoanThu.getId());
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                repository.delete(khoanThu.getId());
                throw new RuntimeException("Lỗi tạo hóa đơn nháp: " + e.getMessage(), e);
            }
        }
        return success;
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
