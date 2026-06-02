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
        return addNotification(thongBao, new java.util.ArrayList<>());
    }

    public boolean addNotification(ThongBao thongBao, List<Integer> groupIds) {
        if (thongBao.getTenThongBao() == null || thongBao.getTenThongBao().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thông báo không được để trống");
        }
        return repository.insert(thongBao, groupIds);
    }

    public boolean updateNotification(ThongBao thongBao) {
        return updateNotification(thongBao, repository.getGroupIdsByNotification(thongBao.getId()));
    }

    public boolean updateNotification(ThongBao thongBao, List<Integer> groupIds) {
        if (thongBao.getTenThongBao() == null || thongBao.getTenThongBao().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thông báo không được để trống");
        }
        return repository.update(thongBao, groupIds);
    }

    public List<Integer> getGroupIdsByNotification(int thongBaoId) {
        return repository.getGroupIdsByNotification(thongBaoId);
    }

    public List<ThongBao> getNotificationsForUser(int userId) {
        return repository.findNotificationsForUser(userId);
    }

    public boolean deleteNotification(int id) {
        return repository.delete(id);
    }

    public List<ThongBao> searchNotifications(String tenThongBao, LocalDate ngayBanHanh, String trangThai) {
        return repository.search(tenThongBao, ngayBanHanh, trangThai, null);
    }

    public List<ThongBao> searchNotifications(String tenThongBao, LocalDate ngayBanHanh, String trangThai,
            Integer groupId) {
        return repository.search(tenThongBao, ngayBanHanh, trangThai, groupId);
    }

    public boolean markAsRead(int userId, int thongBaoId) {
        return repository.markAsRead(userId, thongBaoId);
    }

    public List<Integer> getReadNotificationIds(int userId) {
        return repository.getReadNotificationIds(userId);
    }
}
