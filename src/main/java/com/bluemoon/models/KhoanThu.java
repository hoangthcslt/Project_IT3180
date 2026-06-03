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
    private LocalDate hanNop;
    private String trangThai; // 'DRAFT', 'PUBLISHED'

    public KhoanThu() {}

    public KhoanThu(int id, String maKhoanThu, String tenKhoanThu, String loaiPhi, BigDecimal donGia, LocalDate ngayTao, String ghiChu) {
        this.id = id;
        this.maKhoanThu = maKhoanThu;
        this.tenKhoanThu = tenKhoanThu;
        this.loaiPhi = loaiPhi;
        this.donGia = donGia;
        this.ngayTao = ngayTao;
        this.ghiChu = ghiChu;
        this.trangThai = "DRAFT";
    }

    public KhoanThu(int id, String maKhoanThu, String tenKhoanThu, String loaiPhi, BigDecimal donGia, LocalDate ngayTao, String ghiChu, LocalDate hanNop, String trangThai) {
        this.id = id;
        this.maKhoanThu = maKhoanThu;
        this.tenKhoanThu = tenKhoanThu;
        this.loaiPhi = loaiPhi;
        this.donGia = donGia;
        this.ngayTao = ngayTao;
        this.ghiChu = ghiChu;
        this.hanNop = hanNop;
        this.trangThai = trangThai;
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

    public LocalDate getHanNop() {
        return hanNop;
    }

    public void setHanNop(LocalDate hanNop) {
        this.hanNop = hanNop;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
