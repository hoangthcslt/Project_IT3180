


Chào team, với tư cách là Scrum Master của dự án, tôi đã lên kế hoạch và phân rã các đầu việc dựa trên Requiremets và Database Schema của chúng ta. 

Dự án sẽ được chạy theo mô hình Agile/Scrum, team mình gồm 4 người (tạm gọi là `[Dev A]`, `[Dev B]`, `[Dev C]`, `[Dev D]`). Dưới đây là file `TASKS.md` để mọi người track công việc.

---

# 📋 TASKS.md: Kế hoạch triển khai dự án BlueMoon

## 🚀 GIAI ĐOẠN (PHASE) 1: Setup, Cơ sở dữ liệu & Đăng nhập
*Mục tiêu: Hoàn thiện nền tảng core, kết nối DB thành công và user có thể login vào Dashboard đúng quyền.*

- `[BM-101]` - Khởi tạo CSDL MySQL, chạy file DDL và chèn Seed data mẫu - `[DB]` - `[Dev A]`
- `[BM-102]` - Khởi tạo Project JavaFX, cấu trúc thư mục MVC, tích hợp thư viện (JDBC, BCrypt) - `[Model/Controller]` - `[Dev B]`
- `[BM-103]` - Viết class `DatabaseConnection` (Singleton pattern) và các Base Model - `[Model]` - `[Dev A]`
- `[BM-104]` - Thiết kế UI màn hình Đăng nhập (`Login.fxml`, CSS) - `[UI]` - `[Dev C]`
- `[BM-105]` - Thiết kế UI khung Dashboard chính (Sidebar Menu, Header) - `[UI]` - `[Dev D]`
- `[BM-106]` - Xử lý logic Đăng nhập (Truy vấn DB, check BCrypt, phân quyền, chuyển scene) - `[Controller]` - `[Dev C]`

---

## 🚀 GIAI ĐOẠN (PHASE) 2: Quản lý Nhân khẩu/Hộ khẩu & Tạo khoản thu
*Mục tiêu: Ban quản trị có thể quản lý được cư dân, Kế toán có thể khởi tạo các đợt thu phí.*

- `[BM-201]` - Viết Entity, DAO (Thêm/Sửa/Xóa/Tìm) cho `ho_khau` và `nhan_khau` - `[Model/DB]` - `[Dev A]`
- `[BM-202]` - Thiết kế UI màn hình Quản lý Hộ khẩu & Modal Thêm/Sửa - `[UI]` - `[Dev B]`
- `[BM-203]` - Xử lý logic Quản lý Hộ khẩu (Load TableView, Validate, Insert/Update) - `[Controller]` - `[Dev B]`
- `[BM-204]` - Thiết kế UI màn hình Quản lý Nhân khẩu & Modal khai báo Tạm trú/Vắng - `[UI]` - `[Dev C]`
- `[BM-205]` - Xử lý logic Quản lý Nhân khẩu (Ràng buộc FK Hộ khẩu, Ghi log cư trú) - `[Controller]` - `[Dev C]`
- `[BM-206]` - Viết Entity, DAO cho `khoan_thu` - `[Model/DB]` - `[Dev D]`
- `[BM-207]` - Thiết kế UI màn hình Tạo khoản thu - `[UI]` - `[Dev D]`
- `[BM-208]` - Xử lý logic Tạo khoản thu (Logic quan trọng: Tự động map sinh công nợ cho các hộ nếu là phí BẮT BUỘC) - `[Controller]` - `[Dev A]`

---

## 🚀 GIAI ĐOẠN (PHASE) 3: Chức năng Thu tiền & Thống kê Báo cáo
*Mục tiêu: Đóng vòng đời phần mềm, cho phép ghi nhận dòng tiền và xuất báo cáo.*

- `[BM-301]` - Viết Entity, DAO cho `nop_tien` (Thực hiện giao dịch) - `[Model/DB]` - `[Dev B]`
- `[BM-302]` - Thiết kế UI màn hình Nộp tiền (Tìm kiếm chủ hộ, hiển thị khoản nợ) - `[UI]` - `[Dev C]`
- `[BM-303]` - Xử lý logic Nộp tiền (Validate số tiền, Check trùng lặp, Update trạng thái) - `[Controller]` - `[Dev C]`
- `[BM-304]` - Viết câu lệnh SQL thống kê (SUM, COUNT) & DAO cho Dashboard - `[Model/DB]` - `[Dev A]`
- `[BM-305]` - Thiết kế UI màn hình Thống kê (Vẽ BarChart/PieChart, TableView danh sách nợ) - `[UI]` - `[Dev D]`
- `[BM-306]` - Xử lý logic load Data vào Chart và Filter theo thời gian/loại phí - `[Controller]` - `[Dev D]`
- `[BM-307]` - Tích hợp thư viện Apache POI, xử lý logic Xuất file Excel báo cáo - `[Controller]` - `[Dev B]`

---

## 🔄 WORKFLOW: HƯỚNG DẪN NHẬN TASK & TẠO PULL REQUEST (PR)

Để code base của chúng ta không bị conflict và giữ được chất lượng tốt (không có Code Smell), mọi người tuân thủ luồng làm việc sau dựa theo `CONTRIBUTING.md`:

### Bước 1: Nhận Task (Pick Task)
1. Lên bảng Kanban (Trello/Jira/GitHub Projects).
2. Assign (Gán) tên mình vào Task bạn muốn làm. Kéo thẻ từ cột **TODO** sang **IN PROGRESS**.

### Bước 2: Tạo Branch (Quy tắc đặt tên)
Luôn pull code mới nhất từ nhánh `develop` (hoặc `main`) về trước khi tạo branch mới.
**Cú pháp:** `<loại>/<mã-task>-<tên-viết-tắt>`
*Ví dụ:*
- Làm tính năng: `git checkout -b feature/BM-104-login-ui`
- Fix bug: `git checkout -b bugfix/BM-203-ho-khau-validation`

### Bước 3: Commit Code
Commit thường xuyên, ghi thông điệp rõ ràng:
- `[BM-104] Thiết kế xong giao diện login.fxml`
- `[BM-104] Thêm CSS bo góc cho button đăng nhập`

### Bước 4: Kiểm tra Checklist (BẮT BUỘC TRƯỚC KHI TẠO PR)
Trước khi push branch lên Github và tạo Pull Request merge vào `develop`, hãy tự tick hoàn thành các mục sau:

- [ ] **Build & Run:** Project chạy thành công, không văng lỗi (Crash) ở máy cá nhân.
- [ ] **No Code Smell:** Đã check không có Duplicate Code, biến/hàm đặt tên chuẩn Java (camelCase).
- [ ] **Database Ràng buộc:** Các câu lệnh SQL sử dụng `PreparedStatement` (chống SQL Injection).
- [ ] **UI/UX:** Giao diện không bị vỡ khi resize cửa sổ.
- [ ] **Javadoc:** Đã viết comment mô tả cho các class và method phức tạp.
- [ ] **Merge Conflict:** Đã thử pull code nhánh `develop` về branch hiện tại để tự resolve conflict (nếu có).

### Bước 5: Review & Merge
1. Tạo Pull Request trên GitHub.
2. Tag ít nhất **1 thành viên khác** vào review code.
3. Người review đọc code, nếu OK thì `Approve` và nhấn `Merge pull request`. Nếu chưa OK thì comment yêu cầu sửa (Request Changes).
4. Kéo thẻ task sang cột **DONE**. Done! 🎉