package com.bluemoon.services;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.repositories.ResidentRepository;

import java.time.LocalDate;
import java.util.List;

public class ResidentService {
    private final ResidentRepository repository = new ResidentRepository();

    public List<NhanKhau> getResidentsByHouseholdId(int householdId) { return repository.findByHoKhauId(householdId); }
    public List<NhanKhau> getAllResidents() { return repository.findAll(); }
    public boolean deleteResident(int id) { return repository.delete(id); }

    public boolean addResident(NhanKhau resident) {
        validate(resident, null);
        return repository.insert(resident);
    }

    public boolean updateResident(NhanKhau resident) {
        validate(resident, resident.getId());
        return repository.update(resident);
    }

    public List<NhanKhau> searchResidents(Integer householdId, String name, String cccd, String phone,
            LocalDate birthday, String gender, String relationship, String status) {
        return repository.search(householdId, name, cccd, phone, birthday, gender, relationship, status);
    }

    private void validate(NhanKhau resident, Integer excludedResidentId) {
        if (resident.getHoKhauId() <= 0) throw new IllegalArgumentException("Vui lòng chọn hộ khẩu.");
        if (!hasText(resident.getHoTen())) throw new IllegalArgumentException("Họ tên không được để trống.");
        if (!hasText(resident.getCccd()) || !resident.getCccd().matches("\\d{12}")) throw new IllegalArgumentException("CCCD phải gồm đúng 12 chữ số.");
        if (!hasText(resident.getSoDienThoai()) || !resident.getSoDienThoai().matches("\\d{10,11}")) throw new IllegalArgumentException("SĐT phải gồm 10 hoặc 11 chữ số.");
        if (resident.getNgaySinh() == null) throw new IllegalArgumentException("Ngày sinh không được để trống.");
        if (!hasText(resident.getGioiTinh()) || !hasText(resident.getQuanHe()) || !hasText(resident.getTrangThai())) throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin.");
        if ("Chủ hộ".equals(resident.getQuanHe()) && repository.hasHouseholdHead(resident.getHoKhauId(), excludedResidentId)) {
            throw new IllegalArgumentException("Hộ khẩu này đã có chủ hộ.");
        }
    }

    private boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
}
