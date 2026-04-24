


Chào bạn, với tư cách là một Database Architect của dự án, tôi đã phân tích luồng nghiệp vụ (Business Logic) và các yêu cầu hệ thống (Requirements) để thiết kế file `DATABASE_SCHEMA.md`.

*Lưu ý kiến trúc:* Thay vì tạo bảng trung gian `chi_tiet_ho_khau`, tôi thiết kế quan hệ **1-N** trực tiếp (1 Hộ khẩu có N Nhân khẩu) thông qua khóa ngoại `ho_khau_id` đặt tại bảng `nhan_khau`. Điều này giúp tối ưu hiệu suất truy vấn (JOIN) và tránh dư thừa dữ liệu (Data Redundancy), hoàn toàn phù hợp với thực tế quản lý chung cư. Đối với dữ liệu tiền tệ, tôi sử dụng kiểu `DECIMAL(15,2)` thay vì `DOUBLE` để tránh sai số dấu phẩy động.

Dưới đây là thiết kế chi tiết:

---

# 🗄️ DATABASE_SCHEMA.md: Hệ thống quản lý chung cư BlueMoon

## 1. TỪ ĐIỂN DỮ LIỆU (DATA DICTIONARY)

### 1.1. Bảng `users` (Tài khoản hệ thống)
*Mục đích: Lưu trữ thông tin đăng nhập và phân quyền của nhân viên.*

| Tên trường | Kiểu dữ liệu | Khóa | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | INT | **PK** | AUTO_INCREMENT | ID tự tăng |
| `username` | VARCHAR(50) | | NOT NULL, UNIQUE | Tên đăng nhập |
| `password` | VARCHAR(255) | | NOT NULL | Mật khẩu (Nên hash bằng BCrypt) |
| `role` | ENUM | | 'ADMIN', 'ACCOUNTANT' | Quyền: Ban quản trị / Kế toán |
| `created_at` | TIMESTAMP | | DEFAULT CURRENT_TIMESTAMP| Ngày tạo tài khoản |

### 1.2. Bảng `ho_khau` (Hộ khẩu / Căn hộ)
*Mục đích: Lưu trữ thông tin về căn hộ và chủ hộ.*

| Tên trường | Kiểu dữ liệu | Khóa | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | INT | **PK** | AUTO_INCREMENT | ID tự tăng |
| `ma_ho_khau`| VARCHAR(20) | | NOT NULL, UNIQUE | Mã căn hộ (VD: P101, P202) |
| `ten_chu_ho`| VARCHAR(100) | | NOT NULL | Tên chủ hộ đại diện |
| `dien_tich` | DECIMAL(10,2) | | NOT NULL | Diện tích căn hộ (m2) - dùng tính phí |
| `ngay_lap` | DATE | | NOT NULL | Ngày lập hộ khẩu |

### 1.3. Bảng `nhan_khau` (Nhân khẩu)
*Mục đích: Lưu trữ thông tin chi tiết từng cư dân.*

| Tên trường | Kiểu dữ liệu | Khóa | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | INT | **PK** | AUTO_INCREMENT | ID tự tăng |
| `ho_khau_id`| INT | **FK** | NOT NULL | Thuộc hộ khẩu nào |
| `ho_ten` | VARCHAR(100) | | NOT NULL | Họ và tên |
| `cccd` | VARCHAR(12) | | UNIQUE, NULLABLE | CMND/CCCD (Trẻ em có thể Null) |
| `ngay_sinh` | DATE | | NOT NULL | Ngày sinh |
| `gioi_tinh` | ENUM | | 'NAM', 'NU', 'KHAC' | Giới tính |
| `quan_he` | VARCHAR(50) | | NOT NULL | Quan hệ với chủ hộ (Con, Vợ,...) |
| `trang_thai`| ENUM | | 'THUONG_TRU', 'TAM_TRU', 'TAM_VANG' | Trạng thái cư trú |

### 1.4. Bảng `tam_tru_tam_vang` (Lịch sử tạm trú/tạm vắng)
*Mục đích: Ghi log biến động dân cư.*

| Tên trường | Kiểu dữ liệu | Khóa | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | INT | **PK** | AUTO_INCREMENT | ID tự tăng |
| `nhan_khau_id`| INT| **FK** | NOT NULL | Cư dân nào khai báo |
| `loai_khai_bao`| ENUM| | 'TAM_TRU', 'TAM_VANG'| Loại hình khai báo |
| `tu_ngay` | DATE | | NOT NULL | Từ ngày |
| `den_ngay` | DATE | | NOT NULL | Đến ngày |
| `ly_do` | TEXT | | | Lý do khai báo |

### 1.5. Bảng `khoan_thu` (Định nghĩa khoản thu)
*Mục đích: Thiết lập các đợt thu phí.*

| Tên trường | Kiểu dữ liệu | Khóa | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | INT | **PK** | AUTO_INCREMENT | ID tự tăng |
| `ma_khoan_thu`| VARCHAR(20) | | NOT NULL, UNIQUE | Mã khoản thu (VD: PDV_102023) |
| `ten_khoan_thu`| VARCHAR(200)| | NOT NULL | Tên (VD: Phí dịch vụ Tháng 10) |
| `loai_phi` | ENUM | | 'BAT_BUOC', 'TU_NGUYEN' | Phân loại phí |
| `don_gia` | DECIMAL(15,2) | | DEFAULT 0 | Số tiền (Có thể là phí/m2 hoặc phí fix)|
| `ngay_tao` | DATE | | NOT NULL | Ngày tạo đợt thu |
| `ghi_chu` | TEXT | | NULLABLE | Mô tả chi tiết |

### 1.6. Bảng `nop_tien` (Giao dịch nộp tiền & Công nợ)
*Mục đích: Ghi nhận việc nộp tiền của hộ khẩu đối với một khoản thu.*

| Tên trường | Kiểu dữ liệu | Khóa | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `id` | INT | **PK** | AUTO_INCREMENT | ID tự tăng |
| `ho_khau_id`| INT | **FK** | NOT NULL | Hộ khẩu nộp tiền |
| `khoan_thu_id`| INT| **FK** | NOT NULL | Nộp cho khoản thu nào |
| `nguoi_nop` | VARCHAR(100) | | NOT NULL | Tên người trực tiếp đóng tiền |
| `so_tien_nop`| DECIMAL(15,2) | | NOT NULL | Số tiền đã đóng |
| `ngay_nop` | DATE | | NOT NULL | Ngày giao dịch |
| `hinh_thuc` | ENUM | | 'TIEN_MAT', 'CHUYEN_KHOAN' | Hình thức nộp |

---

## 2. SQL DDL SCRIPT (LỆNH TẠO BẢNG)

*Chạy lệnh này trong MySQL. Thứ tự đã được sắp xếp chuẩn xác để không vi phạm ràng buộc khóa ngoại.*

```sql
CREATE DATABASE IF NOT EXISTS bluemoon_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bluemoon_db;

-- 1. Table users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'ACCOUNTANT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Table ho_khau
CREATE TABLE ho_khau (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ma_ho_khau VARCHAR(20) NOT NULL UNIQUE,
    ten_chu_ho VARCHAR(100) NOT NULL,
    dien_tich DECIMAL(10,2) NOT NULL,
    ngay_lap DATE NOT NULL
) ENGINE=InnoDB;

-- 3. Table khoan_thu
CREATE TABLE khoan_thu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ma_khoan_thu VARCHAR(20) NOT NULL UNIQUE,
    ten_khoan_thu VARCHAR(200) NOT NULL,
    loai_phi ENUM('BAT_BUOC', 'TU_NGUYEN') NOT NULL,
    don_gia DECIMAL(15,2) DEFAULT 0,
    ngay_tao DATE NOT NULL,
    ghi_chu TEXT
) ENGINE=InnoDB;

-- 4. Table nhan_khau
CREATE TABLE nhan_khau (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ho_khau_id INT NOT NULL,
    ho_ten VARCHAR(100) NOT NULL,
    cccd VARCHAR(12) UNIQUE,
    ngay_sinh DATE NOT NULL,
    gioi_tinh ENUM('NAM', 'NU', 'KHAC') NOT NULL,
    quan_he VARCHAR(50) NOT NULL,
    trang_thai ENUM('THUONG_TRU', 'TAM_TRU', 'TAM_VANG') NOT NULL DEFAULT 'THUONG_TRU',
    FOREIGN KEY (ho_khau_id) REFERENCES ho_khau(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Table tam_tru_tam_vang
CREATE TABLE tam_tru_tam_vang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nhan_khau_id INT NOT NULL,
    loai_khai_bao ENUM('TAM_TRU', 'TAM_VANG') NOT NULL,
    tu_ngay DATE NOT NULL,
    den_ngay DATE NOT NULL,
    ly_do TEXT,
    FOREIGN KEY (nhan_khau_id) REFERENCES nhan_khau(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Table nop_tien
CREATE TABLE nop_tien (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ho_khau_id INT NOT NULL,
    khoan_thu_id INT NOT NULL,
    nguoi_nop VARCHAR(100) NOT NULL,
    so_tien_nop DECIMAL(15,2) NOT NULL,
    ngay_nop DATE NOT NULL,
    hinh_thuc ENUM('TIEN_MAT', 'CHUYEN_KHOAN') NOT NULL,
    FOREIGN KEY (ho_khau_id) REFERENCES ho_khau(id) ON DELETE RESTRICT,
    FOREIGN KEY (khoan_thu_id) REFERENCES khoan_thu(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
```
*(Lưu ý Architect: Tại bảng `nop_tien`, tôi đặt `ON DELETE RESTRICT` để bảo vệ dữ liệu tài chính. Không ai được phép xóa Hộ khẩu hoặc Đợt thu phí nếu đã có giao dịch tiền nong liên quan đến nó).*

---

## 3. SQL DML SCRIPT (DỮ LIỆU MẪU ĐỂ TEST)

```sql
USE bluemoon_db;

-- Insert Users (Password đang để plain text '123456' cho dev test, thực tế cần hash)
INSERT INTO users (username, password, role) VALUES 
('admin_blue', '123456', 'ADMIN'),
('ketoan_moon', '123456', 'ACCOUNTANT');

-- Insert Hộ khẩu
INSERT INTO ho_khau (ma_ho_khau, ten_chu_ho, dien_tich, ngay_lap) VALUES 
('P101', 'Nguyễn Văn A', 75.50, '2023-01-15'),
('P102', 'Trần Thị B', 60.00, '2023-02-20');

-- Insert Nhân khẩu
INSERT INTO nhan_khau (ho_khau_id, ho_ten, cccd, ngay_sinh, gioi_tinh, quan_he, trang_thai) VALUES 
(1, 'Nguyễn Văn A', '001090123456', '1990-05-10', 'NAM', 'Chủ hộ', 'THUONG_TRU'),
(1, 'Lê Thị C', '001092654321', '1992-08-15', 'NU', 'Vợ', 'THUONG_TRU'),
(2, 'Trần Thị B', '001085789123', '1985-11-20', 'NU', 'Chủ hộ', 'THUONG_TRU');

-- Insert Khoản thu (1 Bắt buộc tính theo m2, 1 Tự nguyện)
INSERT INTO khoan_thu (ma_khoan_thu, ten_khoan_thu, loai_phi, don_gia, ngay_tao, ghi_chu) VALUES 
('PDV_102023', 'Phí dịch vụ chung cư Tháng 10/2023', 'BAT_BUOC', 7000.00, '2023-10-01', '7.000đ/m2'),
('QTT_2023', 'Quỹ vì người nghèo năm 2023', 'TU_NGUYEN', 0, '2023-10-15', 'Tùy tâm cư dân');

-- Insert Giao dịch nộp tiền
-- Nhà P101 nộp phí dịch vụ (75.5m2 * 7000 = 528,500đ)
INSERT INTO nop_tien (ho_khau_id, khoan_thu_id, nguoi_nop, so_tien_nop, ngay_nop, hinh_thuc) VALUES 
(1, 1, 'Nguyễn Văn A', 528500.00, '2023-10-05', 'CHUYEN_KHOAN');

-- Nhà P102 ủng hộ quỹ từ thiện 200,000đ
INSERT INTO nop_tien (ho_khau_id, khoan_thu_id, nguoi_nop, so_tien_nop, ngay_nop, hinh_thuc) VALUES 
(2, 2, 'Trần Thị B', 200000.00, '2023-10-16', 'TIEN_MAT');
```