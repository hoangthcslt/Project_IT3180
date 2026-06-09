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

    public static double[][] parseDienTiers(String ghiChu) {
        double[][] defaultTiers = {
            {1984.0, 50.0},
            {2050.0, 50.0},
            {2380.0, 100.0},
            {2998.0, 100.0},
            {3350.0, 100.0},
            {3460.0, 0.0}
        };
        
        if (ghiChu == null || ghiChu.trim().isEmpty() || !ghiChu.contains(",") || !ghiChu.contains(";")) {
            return defaultTiers;
        }
        
        try {
            String[] parts = ghiChu.trim().split(";");
            if (parts.length != 6) {
                return defaultTiers;
            }
            double[][] parsed = new double[6][2];
            for (int i = 0; i < 6; i++) {
                String[] subParts = parts[i].split(",");
                parsed[i][0] = Double.parseDouble(subParts[0].trim());
                parsed[i][1] = Double.parseDouble(subParts[1].trim());
            }
            return parsed;
        } catch (Exception e) {
            return defaultTiers;
        }
    }

    public static BigDecimal calculateDienPrice(BigDecimal qty, String ghiChu) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        double[][] tiers = parseDienTiers(ghiChu);
        double qtyVal = qty.doubleValue();
        double totalAmount = 0.0;
        
        for (int i = 0; i < tiers.length; i++) {
            double price = tiers[i][0];
            double limit = tiers[i][1];
            
            if (limit > 0) {
                double usage = Math.min(qtyVal, limit);
                totalAmount += usage * price;
                qtyVal -= usage;
                if (qtyVal <= 0) {
                    break;
                }
            } else {
                totalAmount += qtyVal * price;
                break;
            }
        }
        
        return BigDecimal.valueOf(totalAmount).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return tenPhi + " (" + maPhi + ")";
    }
}
