package com.bluemoon.models;

import java.math.BigDecimal;

public class ChiTietHoaDon {
    private int id;
    private int hoaDonId;
    private String maPhi;
    private String tenPhi;
    private BigDecimal donGia;
    private BigDecimal soLuong;
    private BigDecimal thanhTien;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(int id, int hoaDonId, String maPhi, String tenPhi, BigDecimal donGia, BigDecimal soLuong, BigDecimal thanhTien) {
        this.id = id;
        this.hoaDonId = hoaDonId;
        this.maPhi = maPhi;
        this.tenPhi = tenPhi;
        this.donGia = donGia;
        this.soLuong = soLuong;
        this.thanhTien = thanhTien;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHoaDonId() {
        return hoaDonId;
    }

    public void setHoaDonId(int hoaDonId) {
        this.hoaDonId = hoaDonId;
    }

    public String getMaPhi() {
        return maPhi;
    }

    public void setMaPhi(String maPhi) {
        this.maPhi = maPhi;
    }

    public String getTenPhi() {
        return tenPhi;
    }

    public void setTenPhi(String tenPhi) {
        this.tenPhi = tenPhi;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public BigDecimal getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(BigDecimal soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
}
