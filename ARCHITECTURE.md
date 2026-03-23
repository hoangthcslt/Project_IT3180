# KIẾN TRÚC MVC VÀ CẤU TRÚC THƯ MỤC
## Cấu trúc thư mục dự án (Package Structure)
```text
src/
 ├── application/       # Chứa hàm main() để chạy chương trình
 │    └── Main.java
 ├── models/            # Chứa các class Object (User, HoKhau, KhoanThu...)
 │    ├── KhoanThuModel.java
 │    └── NopTienModel.java
 ├── views/             # Chứa các file giao diện .fxml
 │    ├── Login.fxml
 │    ├── TrangChu.fxml
 │    └── ThemKhoanThu.fxml
 ├── controllers/       # Xử lý logic từ View, gọi xuống Database
 │    ├── LoginController.java
 │    └── KhoanThuController.java
 └── services/          # (Tùy chọn) Chứa các class chuyên viết câu lệnh SQL (SELECT, INSERT, UPDATE, DELETE)
      ├── DatabaseConnection.java
      └── KhoanThuService.java