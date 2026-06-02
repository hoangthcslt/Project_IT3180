package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HoaDon {
    private int id;
    private int khoanThuId;
    private int hoKhauId;
    private String maHoKhau;
    private String tenChuHo;
    private String maHoaDon;
    private BigDecimal tongTien;
    private BigDecimal soTienDaNop;
    private String trangThai; // 'CHUA_NOP', 'DA_NOP'
    private LocalDate ngayTao;
    private LocalDate hanNop;

    public HoaDon() {
    }

    public HoaDon(int id, int khoanThuId, int hoKhauId, String maHoKhau, String tenChuHo, String maHoaDon,
                  BigDecimal tongTien, BigDecimal soTienDaNop, String trangThai, LocalDate ngayTao, LocalDate hanNop) {
        this.id = id;
        this.khoanThuId = khoanThuId;
        this.hoKhauId = hoKhauId;
        this.maHoKhau = maHoKhau;
        this.tenChuHo = tenChuHo;
        this.maHoaDon = maHoaDon;
        this.tongTien = tongTien;
        this.soTienDaNop = soTienDaNop;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.hanNop = hanNop;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKhoanThuId() {
        return khoanThuId;
    }

    public void setKhoanThuId(int khoanThuId) {
        this.khoanThuId = khoanThuId;
    }

    public int getHoKhauId() {
        return hoKhauId;
    }

    public void setHoKhauId(int hoKhauId) {
        this.hoKhauId = hoKhauId;
    }

    public String getMaHoKhau() {
        return maHoKhau;
    }

    public void setMaHoKhau(String maHoKhau) {
        this.maHoKhau = maHoKhau;
    }

    public String getTenChuHo() {
        return tenChuHo;
    }

    public void setTenChuHo(String tenChuHo) {
        this.tenChuHo = tenChuHo;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public BigDecimal getSoTienDaNop() {
        return soTienDaNop;
    }

    public void setSoTienDaNop(BigDecimal soTienDaNop) {
        this.soTienDaNop = soTienDaNop;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDate getHanNop() {
        return hanNop;
    }

    public void setHanNop(LocalDate hanNop) {
        this.hanNop = hanNop;
    }
}
