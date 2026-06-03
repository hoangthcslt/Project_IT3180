package com.bluemoon.services;

import com.bluemoon.models.HoaDon;
import com.bluemoon.models.ChiTietHoaDon;
import com.bluemoon.repositories.HoaDonRepository;

import java.util.List;

public class HoaDonService {
    private final HoaDonRepository repository = new HoaDonRepository();

    public List<HoaDon> getInvoicesByRun(int runId) {
        return repository.findByKhoanThuId(runId);
    }

    public List<ChiTietHoaDon> getInvoiceDetails(int invoiceId) {
        return repository.findDetailsByHoaDonId(invoiceId);
    }

    public boolean saveInvoiceDetails(int invoiceId, List<ChiTietHoaDon> details) {
        for (ChiTietHoaDon det : details) {
            if (det.getSoLuong() == null || det.getSoLuong().signum() < 0) {
                throw new IllegalArgumentException("Số lượng không được nhỏ hơn 0.");
            }
        }
        return repository.saveInvoiceDetails(invoiceId, details);
    }

    public List<HoaDon> searchInvoices(String keyword, String trangThai, String loaiPhi) {
        return repository.searchInvoices(keyword, trangThai, loaiPhi);
    }
}
