package com.bluemoon.models;

public class UserGroup {
    private int id;
    private String tenNhom;
    private String moTa;

    public UserGroup() {}

    public UserGroup(int id, String tenNhom, String moTa) {
        this.id = id;
        this.tenNhom = tenNhom;
        this.moTa = moTa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    @Override
    public String toString() {
        return tenNhom;
    }
}
