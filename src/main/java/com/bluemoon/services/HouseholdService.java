package com.bluemoon.services;

import com.bluemoon.models.HoKhau;
import com.bluemoon.repositories.HouseholdRepository;

import java.util.List;

public class HouseholdService {
    private final HouseholdRepository repository = new HouseholdRepository();

    public List<HoKhau> getAllHouseholds() { return repository.findAll(); }
    public boolean isMaHoKhauExists(String maHoKhau) { return repository.isMaHoKhauExists(maHoKhau); }
    public boolean isMaHoKhauExists(String maHoKhau, int excludedId) { return repository.isMaHoKhauExists(maHoKhau, excludedId); }
    public HoKhau findByMaHoKhau(String maHoKhau) { return hasText(maHoKhau) ? repository.findByMaHoKhau(maHoKhau.trim()) : null; }
    public boolean addHousehold(HoKhau hoKhau) { validate(hoKhau); return repository.insert(hoKhau); }
    public boolean updateHousehold(HoKhau hoKhau) { validate(hoKhau); return repository.update(hoKhau); }
    public boolean deleteHousehold(int id) { return repository.delete(id); }

    public List<HoKhau> searchHouseholds(String ma, String ten, String dienTich, String trangThai,
            String soNguoi, String phuongTien) {
        return repository.search(ma, ten, dienTich, trangThai, soNguoi, phuongTien);
    }

    private void validate(HoKhau hoKhau) {
        if (!hasText(hoKhau.getMaHoKhau())) throw new IllegalArgumentException("Mã hộ khẩu không được để trống");
        if (!hasText(hoKhau.getTenChuHo())) throw new IllegalArgumentException("Tên chủ hộ không được để trống");
        if (hoKhau.getDienTich() == null || hoKhau.getDienTich().signum() <= 0) throw new IllegalArgumentException("Diện tích phải lớn hơn 0");
        if (!hasText(hoKhau.getTrangThai())) throw new IllegalArgumentException("Trạng thái không được để trống");
        if (hoKhau.getSoNguoi() < 0) throw new IllegalArgumentException("Số người không được nhỏ hơn 0");
        if (!hasText(hoKhau.getPhuongTien())) throw new IllegalArgumentException("Phương tiện không được để trống");
        if (hoKhau.getNgayLap() == null) throw new IllegalArgumentException("Ngày lập không được để trống");
    }

    private boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
}
