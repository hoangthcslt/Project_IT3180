# KIẾN TRÚC HỆ THỐNG - BLUEMOON PROJECT

## 1. Mô hình kiến trúc: Layered Architecture (Phân lớp)
Dự án được tổ chức theo mô hình phân lớp để đảm bảo tính dễ bảo trì và mở rộng, mô phỏng luồng xử lý của một ứng dụng Web hiện đại.

- **Presentation Layer (Tầng hiển thị):** Sử dụng JavaFX (.fxml) và Controllers để quản lý giao diện.
- **Business Logic Layer (Tầng nghiệp vụ):** Các Services xử lý tính toán, quy tắc thu phí (bắt buộc/tự nguyện).
- **Data Access Layer (Tầng truy cập dữ liệu):** Các Repositories/DAO thực hiện truy vấn MySQL.
- **Model Layer:** Các thực thể (Entities) đại diện cho bảng trong CSDL.

## 2. Cấu trúc thư mục (Package Structure)
```text
src/
 ├── main/
 │    ├── java/com/bluemoon/
 │    │    ├── application/      # Chứa class chạy Main App
 │    │    ├── controllers/      # Điều hướng UI, nhận input từ người dùng
 │    │    │    ├── NhanKhauController.java
 │    │    │    └── KhoanThuController.java
 │    │    ├── services/         # Chứa Logic nghiệp vụ (Tính phí, kiểm tra điều kiện)
 │    │    │    ├── FeeService.java
 │    │    │    └── ResidentService.java
 │    │    ├── repositories/     # Thao tác trực tiếp với Database (SQL queries)
 │    │    │    ├── UserRepository.java
 │    │    │    └── PaymentRepository.java
 │    │    ├── models/           # Các Plain Old Java Objects (POJO)
 │    │    │    ├── User.java
 │    │    │    └── HoKhau.java
 │    │    └── utils/            # DBConnection, PasswordHasher, Config...
 │    └── resources/
 │         ├── views/            # Chứa file .fxml (Scene Builder)
 │         ├── styles/           # Chứa file .css để làm đẹp giao diện
 │         └── assets/           # Hình ảnh, icons của ứng dụng