package com.bluemoon.models;

import java.time.LocalDate;

public class NhanKhau {
    private int id;
    private int hoKhauId;
    private String hoTen;
    private String cccd;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String quanHe;
    private String trangThai;

    public NhanKhau() {
    }

    public NhanKhau(int id, int hoKhauId, String hoTen, String cccd, String soDienThoai, LocalDate ngaySinh,
            String gioiTinh, String quanHe, String trangThai) {
        this.id = id;
        this.hoKhauId = hoKhauId;
        this.hoTen = hoTen;
        this.cccd = cccd;
        this.soDienThoai = soDienThoai;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.quanHe = quanHe;
        this.trangThai = trangThai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHoKhauId() { return hoKhauId; }
    public void setHoKhauId(int hoKhauId) { this.hoKhauId = hoKhauId; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getQuanHe() { return quanHe; }
    public void setQuanHe(String quanHe) { this.quanHe = quanHe; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
