
# 📄 REQUIREMENTS.md: Phần mềm quản lý thu phí chung cư BlueMoon

## 🎯 Tổng quan hệ thống
- **Công nghệ:** Java, JavaFX (UI), MySQL (Database), MVC Architecture.
- **Tác nhân (Actors):**
  - `Quản trị viên / Ban quản trị (Tổ trưởng/Tổ phó)`: Quản lý nhân khẩu, hộ khẩu, tài khoản hệ thống.
  - `Kế toán`: Quản lý các khoản thu, giao dịch nộp tiền, thống kê và công nợ.

---

## 📦 MODULE 0: XÁC THỰC & PHÂN QUYỀN (Auth)

**User Story:** *Là một người dùng (Ban quản trị/Kế toán), tôi muốn đăng nhập vào hệ thống để sử dụng các chức năng theo đúng thẩm quyền của mình.*

- **Input:** `username` (TextField), `password` (PasswordField).
- **Process:**
  1. Kiểm tra rỗng.
  2. Truy vấn Database bảng `users` khớp thông tin.
  3. Phân quyền: Kiểm tra cột `role` để hiển thị Dashboard tương ứng (Kế toán hoặc Ban quản trị).
- **Output/Error:** 
  - Thành công: Chuyển sang màn hình `Dashboard.fxml`.
  - Thất bại: `Alert (Error)` - "Tài khoản hoặc mật khẩu không chính xác!".
- **UI/UX:** Màn hình đơn sắc, căn giữa, hỗ trợ phím `Enter` để submit.

---

## 📦 MODULE 1: QUẢN LÝ NHÂN KHẨU & HỘ KHẨU (Demographics)

### 1.1 Quản lý Hộ khẩu
**User Story:** *Là Ban quản trị, tôi muốn thêm/sửa/xóa thông tin hộ khẩu để quản lý danh sách các căn hộ trong chung cư.*

- **Input:** `Mã hộ khẩu` (Auto-gen hoặc String), `Tên chủ hộ` (TextField), `Số nhà/Mã căn hộ` (TextField), `Diện tích` (Double - *Dùng để tính phí quản lý*).
- **Process:** Validate trùng `Số nhà/Mã căn hộ`. Insert/Update bảng `ho_khau`. Delete cần kiểm tra ràng buộc khóa ngoại (Không xóa nếu đang có nhân khẩu hoặc nợ phí).
- **Output/Error:** Lỗi trùng lặp dữ liệu, thông báo thành công.
- **UI/UX:** Dùng `TableView` hiển thị danh sách. Có thanh `SearchBar`. Nút Action (Sửa/Xóa) nằm trên từng dòng (TableCell).

### 1.2 Quản lý Nhân khẩu & Cư trú
**User Story:** *Là Ban quản trị, tôi muốn thêm nhân khẩu mới và cập nhật trạng thái cư trú (tạm trú/tạm vắng) để quản lý biến động dân cư.*

- **Input:** `Họ tên`, `CCCD/CMND`, `Ngày sinh` (DatePicker), `Giới tính` (ComboBox), `Quan hệ với chủ hộ` (ComboBox), `Mã hộ khẩu` (ComboBox/Searchable), `Trạng thái cư trú` (Thường trú/Tạm trú/Tạm vắng).
- **Process:** 
  1. Validate CCCD đủ 12 số (nếu có).
  2. Lưu vào bảng `nhan_khau`. Nếu khai báo tạm trú/tạm vắng, ghi log vào bảng `tam_tru_tam_vang`.
- **Output/Error:** Thông báo "Thêm nhân khẩu thành công", Lỗi "CCCD đã tồn tại".
- **UI/UX:** Modal pop-up (`Stage`) nhập liệu để không rời khỏi trang danh sách.

---

## 📦 MODULE 2: QUẢN LÝ KHOẢN THU (Fees)

**User Story:** *Là Kế toán, tôi muốn tạo và quản lý các đợt thu phí (bắt buộc & tự nguyện) để tiến hành thu tiền cư dân.*

- **Input:** 
  - `Tên khoản thu` (TextField).
  - `Loại khoản thu` (ComboBox: Tự nguyện / Bắt buộc).
  - `Mức phí` (TextField dạng số nguyên).
  - `Ghi chú` (TextArea).
- **Process:**
  1. Khởi tạo bản ghi trong bảng `khoan_thu`.
  2. **Logic cốt lõi:** 
     - Nếu là khoản thu **Bắt buộc** (vd: Phí dịch vụ 7000đ/m2), tự động map với toàn bộ danh sách `ho_khau` hiện có, nhân với `Diện tích` căn hộ và tạo ra các bản ghi công nợ rỗng (chưa đóng) trong bảng `nop_tien`.
     - Nếu là khoản thu **Tự nguyện** (vd: Quỹ từ thiện), chỉ tạo khoản thu, không áp công nợ mặc định.
- **Output/Error:** Báo lỗi nếu thiếu trường bắt buộc, nhập chữ vào ô số tiền.
- **UI/UX:** Nút "Tạo khoản thu" màu nổi bật (Primary Button). Dữ liệu list dùng `TableView`.

---

## 📦 MODULE 3: QUẢN LÝ NỘP TIỀN & CÔNG NỢ (Payments & Debts)

### 3.1 Ghi nhận thanh toán
**User Story:** *Là Kế toán, tôi muốn ghi nhận giao dịch nộp tiền của cư dân để cập nhật trạng thái hoàn thành nghĩa vụ tài chính.*

- **Input:** `Mã hộ khẩu / Tên chủ hộ` (Search TextField có Auto-complete), `Mã khoản thu` (ComboBox), `Số tiền nộp` (TextField), `Ngày nộp` (DatePicker mặc định là hôm nay), `Hình thức` (Tiền mặt/Chuyển khoản).
- **Process:**
  1. Query xem Hộ này đã đóng khoản này chưa (chống Duplicate Code).
  2. Với phí tự nguyện: Lưu thẳng số tiền nhập vào.
  3. Với phí bắt buộc: Kiểm tra số tiền đóng phải `>=` số tiền yêu cầu. Đổi trạng thái từ `Chưa nộp` -> `Đã nộp`.
- **Output/Error:** `Alert` - "Hộ này đã đóng khoản phí này rồi!" hoặc "Ghi nhận thành công".
- **UI/UX:** Hiển thị preview Tên chủ hộ và Số tiền cần đóng ngay khi User chọn Mã hộ khẩu.

### 3.2 Theo dõi công nợ
**User Story:** *Là Kế toán, tôi muốn xem danh sách các hộ chưa đóng phí bắt buộc để tiến hành nhắc nhở.*

- **Input:** `Filter theo Mã khoản thu` (ComboBox).
- **Process:** `SELECT` các hộ khẩu chưa có record trong bảng `nop_tien` ứng với `Mã khoản thu` hiện tại.
- **Output/Error:** Danh sách chi tiết các hộ nợ phí.
- **UI/UX:** `TableView` có highlight màu đỏ (`-fx-background-color: #ffe6e6;`) cho các hộ quá hạn nộp.

---

## 📦 MODULE 4: BÁO CÁO & THỐNG KÊ (Reports & Statistics)

**User Story:** *Là Kế toán/Ban quản trị, tôi muốn xem thống kê trực quan và xuất file báo cáo để nắm bắt tình hình tài chính của chung cư.*

- **Input:** `Khoảng thời gian` (Từ ngày - Đến ngày), `Loại khoản thu` (Dropdown).
- **Process:**
  1. Dùng SQL Aggregate functions (`SUM`, `COUNT`) kết hợp `GROUP BY` để tính: Tổng tiền đã thu, Tổng số hộ đã nộp, Tổng số hộ chưa nộp.
  2. Tạo bộ lọc theo Tiêu chí (Tất cả / Bắt buộc / Tự nguyện).
- **Output/Error:** Dữ liệu tính toán trả về View. Nếu không có dữ liệu trả về thông báo "Không có dữ liệu trong thời gian này".
- **UI/UX:** 
  - Phần Dashboard: Dùng `BarChart` hoặc `PieChart` của JavaFX để hiển thị tỷ lệ đã nộp / chưa nộp.
  - Phần Data: Dùng `TableView` liệt kê chi tiết.
  - Có nút "Export Excel" (Sử dụng thư viện Apache POI để xuất file `.xlsx`).

---

## 🛠 HƯỚNG DẪN CHO AI CODING AGENT
1. **Kiến trúc:** Tuân thủ tuyệt đối cấu trúc MVC (`models/`, `views/`, `controllers/`, `services/`, `utils/`).
2. **Giao diện:** File `.fxml` không được chứa Logic, xử lý Event hoàn toàn trong `Controller`.
3. **Database:** Sử dụng `PreparedStatement` trong toàn bộ các câu lệnh SQL để tránh SQL Injection.
4. **Code Quality:** Không áp dụng các "Code smell" (Đặc biệt chú ý *Duplicate Code* và *Shotgun Surgery*). Sử dụng mô hình OOP chuẩn, tận dụng tối đa kế thừa và đa hình. Mọi method phải có Javadoc.