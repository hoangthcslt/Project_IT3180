package com.bluemoon.models;

import java.math.BigDecimal;

public class DanhMucPhi {
    private int id;
    private String maPhi;
    private String tenPhi;
    private String loaiPhi; // 'BAT_BUOC', 'TU_NGUYEN'
    private String loaiTinhGia; // 'CO_DINH', 'THEO_DIEN_TICH', 'THEO_SO_NGUOI', 'NHAP_TAY'
    private BigDecimal donGia;
    private String ghiChu;

    public DanhMucPhi() {
    }

    public DanhMucPhi(int id, String maPhi, String tenPhi, String loaiPhi, String loaiTinhGia, BigDecimal donGia, String ghiChu) {
        this.id = id;
        this.maPhi = maPhi;
        this.tenPhi = tenPhi;
        this.loaiPhi = loaiPhi;
        this.loaiTinhGia = loaiTinhGia;
        this.donGia = donGia;
        this.ghiChu = ghiChu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getLoaiPhi() {
        return loaiPhi;
    }

    public void setLoaiPhi(String loaiPhi) {
        this.loaiPhi = loaiPhi;
    }

    public String getLoaiTinhGia() {
        return loaiTinhGia;
    }

    public void setLoaiTinhGia(String loaiTinhGia) {
        this.loaiTinhGia = loaiTinhGia;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return tenPhi + " (" + maPhi + ")";
    }
}
