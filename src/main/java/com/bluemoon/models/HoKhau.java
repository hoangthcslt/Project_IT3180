package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HoKhau {
    private int id;
    private String maHoKhau;
    private String tenChuHo;
    private BigDecimal dienTich;
    private String trangThai;
    private int soNguoi;
    private String phuongTien;
    private LocalDate ngayLap;
    private int soXeMay;
    private int soOto;

    public HoKhau() {
    }

    public HoKhau(int id, String maHoKhau, String tenChuHo, BigDecimal dienTich, String trangThai,
            int soNguoi, String phuongTien, LocalDate ngayLap) {
        this.id = id;
        this.maHoKhau = maHoKhau;
        this.tenChuHo = tenChuHo;
        this.dienTich = dienTich;
        this.trangThai = trangThai;
        this.soNguoi = soNguoi;
        this.phuongTien = phuongTien;
        this.ngayLap = ngayLap;
        this.soXeMay = 0;
        this.soOto = 0;
    }

    public HoKhau(int id, String maHoKhau, String tenChuHo, BigDecimal dienTich, String trangThai,
            int soNguoi, String phuongTien, LocalDate ngayLap, int soXeMay, int soOto) {
        this.id = id;
        this.maHoKhau = maHoKhau;
        this.tenChuHo = tenChuHo;
        this.dienTich = dienTich;
        this.trangThai = trangThai;
        this.soNguoi = soNguoi;
        this.phuongTien = phuongTien;
        this.ngayLap = ngayLap;
        this.soXeMay = soXeMay;
        this.soOto = soOto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMaHoKhau() { return maHoKhau; }
    public void setMaHoKhau(String maHoKhau) { this.maHoKhau = maHoKhau; }
    public String getTenChuHo() { return tenChuHo; }
    public void setTenChuHo(String tenChuHo) { this.tenChuHo = tenChuHo; }
    public BigDecimal getDienTich() { return dienTich; }
    public void setDienTich(BigDecimal dienTich) { this.dienTich = dienTich; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public int getSoNguoi() { return soNguoi; }
    public void setSoNguoi(int soNguoi) { this.soNguoi = soNguoi; }
    public String getPhuongTien() { return phuongTien; }
    public void setPhuongTien(String phuongTien) { this.phuongTien = phuongTien; }
    public LocalDate getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap) { this.ngayLap = ngayLap; }
    public int getSoXeMay() { return soXeMay; }
    public void setSoXeMay(int soXeMay) { this.soXeMay = soXeMay; }
    public int getSoOto() { return soOto; }
    public void setSoOto(int soOto) { this.soOto = soOto; }
}
