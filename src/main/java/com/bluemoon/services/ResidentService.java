package com.bluemoon.services;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.repositories.ResidentRepository;

import java.util.List;

public class ResidentService {
    private final ResidentRepository repository;

    public ResidentService() {
        this.repository = new ResidentRepository();
    }

    public List<NhanKhau> getResidentsByHouseholdId(int hoKhauId) {
        return repository.findByHoKhauId(hoKhauId);
    }

    public List<NhanKhau> getAllResidents() {
        return repository.findAll();
    }

    public boolean addResident(NhanKhau nhanKhau) {
        if (nhanKhau.getHoTen() == null || nhanKhau.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên nhân khẩu không được để trống");
        }
        return repository.insert(nhanKhau);
    }
}
