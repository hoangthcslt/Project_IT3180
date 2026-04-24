#### 👨‍💻 Prompt dành cho Dev 1 (Phase 1: Setup & Đăng nhập)
> **Nhiệm vụ:** Hoàn thành Phase 1 (Đăng nhập & Khung Dashboard).
> **Bối cảnh:** Hãy đọc file `@DATABASE_SCHEMA.md` và luật kiến trúc ở `@.cursorrules`. Sử dụng `@DBConnection.java` và `@SessionManager.java` đã có sẵn.
> **Các bước thực hiện:**
> 1. Xây dựng class `User` trong `models`.
> 2. Viết `UserRepository` với hàm `getUserByUsername`.
> 3. Viết `AuthService` chứa hàm `authenticate(username, plainPassword)`. Nhớ dùng thư viện jBCrypt để `BCrypt.checkpw`. Lưu User vào `SessionManager` nếu đúng.
> 4. Tạo `login.fxml` và `LoginController`. Nếu login sai hiện Alert. Nếu login đúng, chuyển sang `dashboard.fxml`.
> 5. Tạo `dashboard.fxml` và `DashboardController`. Layout gồm Sidebar bên trái chứa các nút (Hộ khẩu, Nhân khẩu, Khoản thu, Nộp tiền, Thống kê) nhưng hiện tại bấm chưa có tác dụng gì, và một vùng nội dung trống bên phải.

*(Đợi Dev 1 làm xong, tạo PR, bạn Approve gộp vào `main`. Sau đó bảo Dev 2, Dev 3 gõ lệnh `git pull origin main` về rồi mới bắt đầu làm).*

#### 👩‍💻 Prompt dành cho Dev 2 (Phase 2: Nhân khẩu/Hộ khẩu & Khoản thu)
> **Nhiệm vụ:** Hoàn thành Phase 2 (Quản lý Hộ khẩu & Tạo đợt thu phí).
> **Bối cảnh:** Đọc kỹ `@DATABASE_SCHEMA.md` và `@.cursorrules`. Tuân thủ mô hình Controller -> Service -> Repository.
> **Các bước thực hiện:**
> 1. Tạo các Models: `HoKhau`, `NhanKhau`, `KhoanThu`.
> 2. Tạo `HouseholdRepository`, `ResidentRepository`, `FeeRepository`. Dùng `PreparedStatement`.
> 3. Tạo `HouseholdService`, `ResidentService`, `FeeService`.
> 4. **Đặc biệt lưu ý `FeeService.taoKhoanThu()`**: Logic cực kỳ quan trọng. Nhận vào tham số là Object `KhoanThu`. 
>    - Gọi `FeeRepository.insertKhoanThu()` lấy về `khoanThuId`.
>    - Nếu `loai_phi == 'BAT_BUOC'`, lấy danh sách toàn bộ Hộ Khẩu. Tính `so_tien = dien_tich * don_gia`. Rồi gọi `PaymentRepository.insertCongNo()` (tạo sẵn nợ cho họ).
> 5. Tạo các file giao diện `hokhau.fxml`, `khoanthu.fxml` và các Controller tương ứng. Dùng TableView để hiển thị.

#### 🧑‍💻 Prompt dành cho Dev 3 (Phase 3: Thu tiền & Thống kê)
> **Nhiệm vụ:** Hoàn thành Phase 3 (Nộp tiền và Báo cáo).
> **Bối cảnh:** Đọc kỹ `@DATABASE_SCHEMA.md` và `@.cursorrules`. Giả định Phase 2 đã tạo xong các bảng.
> **Các bước thực hiện:**
> 1. Viết `PaymentRepository` chứa hàm `thucHienThanhToan(idHoKhau, idKhoanThu, soTien, hinhThuc)` thực hiện UPDATE bảng `nop_tien`.
> 2. Viết `PaymentService` để Validate: Kiểm tra số tiền nhập vào có >= số tiền nợ không? Chống nhập chữ (NumberFormatException).
> 3. Xây dựng `thanhtoan.fxml` và `PaymentController`. Cho phép tìm kiếm chủ hộ, chọn khoản thu, và hiện ra số tiền cần đóng.
> 4. Viết `ThongKeRepository` chứa các câu lệnh SQL dùng `SUM()` và `GROUP BY` để lấy dữ liệu nợ và đã nộp theo đợt thu phí.
> 5. Xây dựng `thongke.fxml` và `ThongKeController`. Dùng `BarChart` và `PieChart` của JavaFX để vẽ biểu đồ trực quan. Thêm nút "Xuất Excel" dùng thư viện Apache POI (tạo file `ExcelExporter.java` trong `utils`).

---