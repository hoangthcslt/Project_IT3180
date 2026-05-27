package com.bluemoon.models;

import java.time.LocalDate;

public class ThongBao {
    private int id;
    private String tenThongBao;
    private String filePath;
    private LocalDate ngayBanHanh;
    private String trangThai;

    public ThongBao() {
    }

    public ThongBao(int id, String tenThongBao, String filePath, LocalDate ngayBanHanh, String trangThai) {
        this.id = id;
        this.tenThongBao = tenThongBao;
        this.filePath = filePath;
        this.ngayBanHanh = ngayBanHanh;
        this.trangThai = trangThai;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenThongBao() {
        return tenThongBao;
    }

    public void setTenThongBao(String tenThongBao) {
        this.tenThongBao = tenThongBao;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDate getNgayBanHanh() {
        return ngayBanHanh;
    }

    public void setNgayBanHanh(LocalDate ngayBanHanh) {
        this.ngayBanHanh = ngayBanHanh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
