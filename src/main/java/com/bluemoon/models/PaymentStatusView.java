package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentStatusView {
    private int id;
    private String maHoKhau;
    private String tenChuHo;
    private String tenKhoanThu;
    private BigDecimal soTien;
    private LocalDate ngayNop;
    private LocalDate hanDong;
    private String trangThai;

    public PaymentStatusView(int id, String maHoKhau, String tenChuHo, String tenKhoanThu,
                             BigDecimal soTien, LocalDate ngayNop, LocalDate hanDong, String trangThai) {
        this.id = id;
        this.maHoKhau = maHoKhau;
        this.tenChuHo = tenChuHo;
        this.tenKhoanThu = tenKhoanThu;
        this.soTien = soTien;
        this.ngayNop = ngayNop;
        this.hanDong = hanDong;
        this.trangThai = trangThai;
    }

    public int getId() { return id; }
    public String getMaHoKhau() { return maHoKhau; }
    public String getTenChuHo() { return tenChuHo; }
    public String getTenKhoanThu() { return tenKhoanThu; }
    public BigDecimal getSoTien() { return soTien; }
    public LocalDate getNgayNop() { return ngayNop; }
    public LocalDate getHanDong() { return hanDong; }
    public String getTrangThai() { return trangThai; }
}
