package com.bluemoon.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhanAnh {
    private int id;
    private String tieuDe;
    private String linhVuc;
    private String noiDung;
    private List<String> hinhAnh;
    private LocalDate ngayGui;
    private String trangThai;
    private String nguoiGui;
    private String nguoiPhuTrach;
    private String phanHoi;

    public PhanAnh() {
        this.hinhAnh = new ArrayList<>();
    }

    public PhanAnh(int id, String tieuDe, String linhVuc, String noiDung, List<String> hinhAnh,
            LocalDate ngayGui, String trangThai, String nguoiGui, String nguoiPhuTrach, String phanHoi) {
        this.id = id;
        this.tieuDe = tieuDe;
        this.linhVuc = linhVuc;
        this.noiDung = noiDung;
        this.hinhAnh = hinhAnh != null ? hinhAnh : new ArrayList<>();
        this.ngayGui = ngayGui;
        this.trangThai = trangThai;
        this.nguoiGui = nguoiGui;
        this.nguoiPhuTrach = nguoiPhuTrach;
        this.phanHoi = phanHoi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getLinhVuc() {
        return linhVuc;
    }

    public void setLinhVuc(String linhVuc) {
        this.linhVuc = linhVuc;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public List<String> getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(List<String> hinhAnh) {
        this.hinhAnh = hinhAnh != null ? hinhAnh : new ArrayList<>();
    }

    public LocalDate getNgayGui() {
        return ngayGui;
    }

    public void setNgayGui(LocalDate ngayGui) {
        this.ngayGui = ngayGui;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNguoiGui() {
        return nguoiGui;
    }

    public void setNguoiGui(String nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public String getNguoiPhuTrach() {
        return nguoiPhuTrach;
    }

    public void setNguoiPhuTrach(String nguoiPhuTrach) {
        this.nguoiPhuTrach = nguoiPhuTrach;
    }

    public String getPhanHoi() {
        return phanHoi;
    }

    public void setPhanHoi(String phanHoi) {
        this.phanHoi = phanHoi;
    }
}
