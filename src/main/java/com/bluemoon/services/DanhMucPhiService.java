package com.bluemoon.services;

import com.bluemoon.models.DanhMucPhi;
import com.bluemoon.repositories.DanhMucPhiRepository;

import java.util.List;

public class DanhMucPhiService {
    private final DanhMucPhiRepository repository = new DanhMucPhiRepository();

    public List<DanhMucPhi> getAllDanhMucPhi() {
        return repository.findAll();
    }

    public boolean isMaPhiExists(String maPhi) {
        return repository.isMaPhiExists(maPhi);
    }

    public boolean addDanhMucPhi(DanhMucPhi phi) {
        if (phi.getMaPhi() == null || phi.getMaPhi().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phí không được để trống.");
        }
        if (phi.getTenPhi() == null || phi.getTenPhi().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phí không được để trống.");
        }
        if (phi.getDonGia() == null || phi.getDonGia().signum() < 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn hoặc bằng 0.");
        }
        return repository.insert(phi);
    }

    public boolean updateDanhMucPhi(DanhMucPhi phi) {
        if (phi.getTenPhi() == null || phi.getTenPhi().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phí không được để trống.");
        }
        if (phi.getDonGia() == null || phi.getDonGia().signum() < 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn hoặc bằng 0.");
        }
        return repository.update(phi);
    }

    public boolean deleteDanhMucPhi(int id) {
        return repository.delete(id);
    }

    public List<DanhMucPhi> searchDanhMucPhi(String ma, String ten, String loai, String loaiTinh) {
        return repository.search(ma, ten, loai, loaiTinh);
    }
}
