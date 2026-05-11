# KIẾN TRÚC HỆ THỐNG - BLUEMOON PROJECT

## 1. Mô hình kiến trúc: Layered Architecture (Phân lớp)
Dự án được tổ chức theo mô hình phân lớp để đảm bảo tính dễ bảo trì và mở rộng, mô phỏng luồng xử lý của một ứng dụng Web hiện đại.

- **Presentation Layer (Tầng hiển thị):** Sử dụng JavaFX (.fxml) và Controllers để quản lý giao diện.
- **Business Logic Layer (Tầng nghiệp vụ):** Các Services xử lý tính toán, quy tắc thu phí (bắt buộc/tự nguyện).
- **Data Access Layer (Tầng truy cập dữ liệu):** Các Repositories/DAO thực hiện truy vấn MySQL.
- **Model Layer:** Các thực thể (Entities) đại diện cho bảng trong CSDL.

## 2. Cấu trúc thư mục (Package Structure)
```text
BlueMoonProject/
├── pom.xml                       <-- Quản lý thư viện (MySQL, BCrypt, POI, JavaFX)
├── .cursorrules                  <-- Luật cho AI Agent
├── DATABASE_SCHEMA.md            <-- Mô tả CSDL cho AI
├── src/main/java/com/bluemoon/
│   ├── application/
│   │   └── Main.java             <-- Điểm khởi chạy App và load giao diện Login
│   ├── controllers/              <-- Nhận sự kiện từ UI, gọi Service (VD: LoginController)
│   ├── services/                 <-- Xử lý Logic (VD: Kiểm tra tính toán tiền, map hộ khẩu...)
│   ├── repositories/             <-- Chỉ chứa SQL: SELECT, INSERT, UPDATE, DELETE (Thay cho DAO)
│   ├── models/                   <-- Entities (User, HoKhau, KhoanThu...)
│   └── utils/                    
│       ├── DatabaseConnection.java <-- Singleton kết nối DB
│       └── PasswordHasher.java     <-- Hàm mã hóa/kiểm tra mật khẩu BCrypt
│       └── SessionManager.java     <-- Lưu thông tin User đang đăng nhập
└── src/main/resources/
    ├── views/                    <-- Chứa toàn bộ giao diện .fxml
    ├── styles/                   <-- Chứa css (như thành viên của bạn đặt tên là styles cũng rất chuẩn)
    ├── images/                   <-- Logo, icon
    └── database/                 <-- Chứa file schema.sql và seed.sql để team dễ setup DB