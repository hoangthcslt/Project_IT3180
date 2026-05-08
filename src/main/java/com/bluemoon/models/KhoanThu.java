package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class KhoanThu {
    private int id;
    private String maKhoanThu;
    private String tenKhoanThu;
    private String loaiPhi; // 'BAT_BUOC', 'TU_NGUYEN'
    private BigDecimal donGia;
    private LocalDate ngayTao;
    private String ghiChu;

    public KhoanThu() {}

    public KhoanThu(int id, String maKhoanThu, String tenKhoanThu, String loaiPhi, BigDecimal donGia, LocalDate ngayTao, String ghiChu) {
        this.id = id;
        this.maKhoanThu = maKhoanThu;
        this.tenKhoanThu = tenKhoanThu;
        this.loaiPhi = loaiPhi;
        this.donGia = donGia;
        this.ngayTao = ngayTao;
        this.ghiChu = ghiChu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaKhoanThu() {
        return maKhoanThu;
    }

    public void setMaKhoanThu(String maKhoanThu) {
        this.maKhoanThu = maKhoanThu;
    }

    public String getTenKhoanThu() {
        return tenKhoanThu;
    }

    public void setTenKhoanThu(String tenKhoanThu) {
        this.tenKhoanThu = tenKhoanThu;
    }

    public String getLoaiPhi() {
        return loaiPhi;
    }

    public void setLoaiPhi(String loaiPhi) {
        this.loaiPhi = loaiPhi;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
