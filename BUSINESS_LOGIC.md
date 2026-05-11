# NGHIỆP VỤ & CƠ SỞ DỮ LIỆU (DATABASE)

## 1. Các vai trò (Actors)
*   **Tổ trưởng/Tổ phó:** Có toàn quyền hệ thống. Quản lý cư dân, phân quyền tài khoản.
*   **Kế toán:** Quản lý các khoản thu phí, đi thu tiền và làm báo cáo thống kê.

## 2. Các quy tắc nghiệp vụ quan trọng (Business Rules)
*   **Khoản thu bắt buộc:** Bắt buộc mọi hộ phải đóng (Ví dụ: Tiền vệ sinh). Có mức giá cố định (VD: 6.000đ/người/tháng). Tổng tiền = Mức giá x Số người trong hộ.
*   **Khoản thu tự nguyện (Đóng góp):** Không ép buộc. Số tiền tùy tâm (Ví dụ: Quỹ vì người nghèo, Ủng hộ miền Trung...).
*   Mỗi `Hộ khẩu` bắt buộc phải có 1 `Chủ hộ`.

## 3. Thiết kế CSDL (Database Schema)
Hệ thống gồm 5 bảng chính cần quan tâm:
1.  **users**: `id` (PK), `username`, `password`, `vaitro`.
2.  **ho_khau**: `MaHo` (PK), `SoThanhVien`, `DiaChi`.
3.  **nhan_khau**: `id` (PK), `hoten`, `ngaysinh`, `gioitinh`, `cccd`, `MaHo` (FK).
4.  **khoan_thu**: `MaKhoanThu` (PK), `TenKhoanThu`, `SoTien` (Định mức), `LoaiKhoanThu` (0: Bắt buộc, 1: Tự nguyện).
5.  **nop_tien**: `IDNopTien` (PK), `MaHo` (FK), `MaKhoanThu` (FK), `NgayThu`, `SoTien`, `NguoiNop`.