src/
 ├── main/
 │    ├── java/com/bluemoon/
 │    │
 │    │    ├── application/      
 │    │    │    ├── MainApp.java        # Entry point, khởi động JavaFX
 │    │    │    └── AppNavigator.java   # Điều hướng scene (chuyển màn hình)
 │    │    │
 │    │    ├── config/                 
 │    │    │    ├── DatabaseConfig.java # Cấu hình kết nối DB (URL, user, password)
 │    │    │    └── AppConfig.java      # Cấu hình chung (constants, env)
 │    │    │
 │    │    ├── controllers/            
 │    │    │    ├── LoginController.java      # Xử lý đăng nhập
 │    │    │    ├── DashboardController.java # Màn hình chính
 │    │    │    ├── NhanKhauController.java  # Quản lý cư dân
 │    │    │    ├── HoKhauController.java    # Quản lý hộ khẩu
 │    │    │    ├── KhoanThuController.java  # Quản lý khoản thu
 │    │    │    └── ThanhToanController.java # Thanh toán
 │    │    │
 │    │    ├── services/               
 │    │    │    ├── AuthService.java        # Logic đăng nhập
 │    │    │    ├── ResidentService.java   # Logic cư dân
 │    │    │    ├── HouseholdService.java  # Logic hộ khẩu
 │    │    │    ├── FeeService.java        # Tính phí
 │    │    │    └── PaymentService.java    # Xử lý thanh toán
 │    │    │
 │    │    ├── repositories/           
 │    │    │    ├── UserRepository.java      # Query bảng user
 │    │    │    ├── ResidentRepository.java  # Query cư dân
 │    │    │    ├── HouseholdRepository.java # Query hộ khẩu
 │    │    │    ├── FeeRepository.java       # Query khoản thu
 │    │    │    ├── InvoiceRepository.java   # Query hóa đơn
 │    │    │    └── PaymentRepository.java   # Query thanh toán
 │    │    │
 │    │    ├── models/                 
 │    │    │    ├── User.java       # Admin/user hệ thống
 │    │    │    ├── HoKhau.java     # Hộ khẩu
 │    │    │    ├── NhanKhau.java   # Cư dân
 │    │    │    ├── KhoanThu.java   # Loại phí
 │    │    │    ├── HoaDon.java     # Hóa đơn
 │    │    │    └── ThanhToan.java  # Thanh toán
 │    │    │
 │    │    ├── utils/              
 │    │    │    ├── DBConnection.java   # Tạo connection DB
 │    │    │    ├── PasswordHasher.java# Hash password
 │    └── resources/
 │         ├── views/             
 │         │    ├── login.fxml
 │         │    ├── dashboard.fxml
 │         │    ├── nhankhau.fxml
 │         │    ├── hokhau.fxml
 │         │    ├── khoanthu.fxml
 │         │    └── thanhtoan.fxml
 │         │
 │         ├── styles/            
 │         │    └── main.css
 │         │
 │         ├── assets/            
 │         │  └── icons, images
 │       
 └── database/
      ├── schema.sql  # Tạo bảng
      └── seed.sql    # Dữ liệu mẫu



controller UI
Service logic
repository
model