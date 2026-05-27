package com.bluemoon.services;

import com.bluemoon.models.ThongBao;
import com.bluemoon.repositories.ThongBaoRepository;

import java.time.LocalDate;
import java.util.List;

public class ThongBaoService {
    private final ThongBaoRepository repository;

    public ThongBaoService() {
        this.repository = new ThongBaoRepository();
    }

    public List<ThongBao> getAllNotifications() {
        return repository.findAll();
    }

    public boolean addNotification(ThongBao thongBao) {
        if (thongBao.getTenThongBao() == null || thongBao.getTenThongBao().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thông báo không được để trống");
        }
        return repository.insert(thongBao);
    }

    public boolean updateNotification(ThongBao thongBao) {
        if (thongBao.getTenThongBao() == null || thongBao.getTenThongBao().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thông báo không được để trống");
        }
        return repository.update(thongBao);
    }

    public boolean deleteNotification(int id) {
        return repository.delete(id);
    }

    public List<ThongBao> searchNotifications(String tenThongBao, LocalDate ngayBanHanh, String trangThai) {
        return repository.search(tenThongBao, ngayBanHanh, trangThai);
    }
}
