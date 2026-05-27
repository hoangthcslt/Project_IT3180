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

    public HoKhau findByMaHoKhau(String maHoKhau) {
        if (maHoKhau == null || maHoKhau.trim().isEmpty())
            return null;
        return repository.findByMaHoKhau(maHoKhau.trim());
    }

    public boolean addHousehold(HoKhau hoKhau) {
        // Có thể thêm business logic validation ở đây nếu cần
        if (hoKhau.getMaHoKhau() == null || hoKhau.getMaHoKhau().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hộ khẩu không được để trống");
        }
        return repository.insert(hoKhau);
    }

    public boolean updateHousehold(HoKhau hoKhau) {
        if (hoKhau.getMaHoKhau() == null || hoKhau.getMaHoKhau().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hộ khẩu không được để trống");
        }
        return repository.update(hoKhau);
    }

    public boolean deleteHousehold(int id) {
        return repository.delete(id);
    }

    public List<HoKhau> searchHouseholds(String maHoKhau, String tenChuHo, String dienTich,
            java.time.LocalDate ngayLap) {
        return repository.search(maHoKhau, tenChuHo, dienTich, ngayLap);
    }
}
