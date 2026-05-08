package com.bluemoon.services;

import com.bluemoon.models.HoKhau;
import com.bluemoon.repositories.HouseholdRepository;

import java.util.List;

public class HouseholdService {
    private final HouseholdRepository repository;

    public HouseholdService() {
        this.repository = new HouseholdRepository();
    }

    public List<HoKhau> getAllHouseholds() {
        return repository.findAll();
    }

    public boolean isMaHoKhauExists(String maHoKhau) {
        return repository.isMaHoKhauExists(maHoKhau);
    }

    public boolean addHousehold(HoKhau hoKhau) {
        // Có thể thêm business logic validation ở đây nếu cần
        if (hoKhau.getMaHoKhau() == null || hoKhau.getMaHoKhau().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hộ khẩu không được để trống");
        }
        return repository.insert(hoKhau);
    }
}
