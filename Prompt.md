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
> 4. **Lưu ý về Khoản thu và Công nợ**: Không cần gọi hàm tạo sẵn nợ (`insertCongNo`) khi tạo đợt thu phí. Bảng `nop_tien` chỉ dùng để lưu "Giao dịch thực tế". Số tiền nợ sẽ được tính toán động (Dynamic) trong lúc truy vấn SQL (`diện tích * đơn giá - SUM(tiền đã nộp)`).
> 5. Tạo các file giao diện `hokhau.fxml`, `khoanthu.fxml` và các Controller tương ứng. Dùng TableView để hiển thị. **Chú ý:** Code Controller sao cho các giao diện này được load vào phần `Center` của `dashboard.fxml` thay vì mở cửa sổ (`Stage`) mới.

#### 🧑‍💻 Prompt dành cho Dev 3 (Phase 3: Thu tiền & Thống kê)
> **Nhiệm vụ:** Hoàn thành Phase 3 (Nộp tiền và Báo cáo).
> **Bối cảnh:** Đọc kỹ `@DATABASE_SCHEMA.md` và `@.cursorrules`. Giả định Phase 2 đã tạo xong các bảng.
> **Các bước thực hiện:**
> 1. Viết `PaymentRepository` chứa hàm `thucHienThanhToan(idHoKhau, idKhoanThu, soTien, hinhThuc, nguoiNop)` thực hiện **INSERT INTO** bảng `nop_tien`. (Lưu ý: Bảng này là lịch sử giao dịch, cư dân có thể đóng nhiều lần, do đó tuyệt đối dùng INSERT, KHÔNG dùng UPDATE).
> 2. Viết `PaymentService` để Validate: Kiểm tra số tiền nộp có hợp lệ không (Lớn hơn 0 và không vượt quá số nợ còn lại)? Chống nhập chữ (NumberFormatException).
> 3. Xây dựng `thanhtoan.fxml` và `PaymentController`. Cho phép tìm kiếm chủ hộ, chọn khoản thu, và hiện ra số tiền cần đóng. Các UI này cũng phải được load vào `Center` của Dashboard.
> 4. Viết `ThongKeRepository` chứa các câu lệnh SQL dùng `SUM()` và `GROUP BY` để lấy dữ liệu nợ và đã nộp theo đợt thu phí.
> 5. Xây dựng `thongke.fxml` và `ThongKeController`. Dùng `BarChart` và `PieChart` của JavaFX để vẽ biểu đồ trực quan. Thêm nút "Xuất Excel" dùng thư viện Apache POI (tạo file `ExcelExporter.java` trong `utils`).

---