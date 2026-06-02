CREATE DATABASE IF NOT EXISTS bluemoon_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bluemoon_db;

-- 1. Table users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'ACCOUNTANT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Table ho_khau
CREATE TABLE IF NOT EXISTS ho_khau (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ma_ho_khau VARCHAR(20) NOT NULL UNIQUE,
    ten_chu_ho VARCHAR(100) NOT NULL,
    dien_tich DECIMAL(10,2) NOT NULL,
    ngay_lap DATE NOT NULL
) ENGINE=InnoDB;

-- 3. Table khoan_thu
CREATE TABLE IF NOT EXISTS khoan_thu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ma_khoan_thu VARCHAR(20) NOT NULL UNIQUE,
    ten_khoan_thu VARCHAR(200) NOT NULL,
    loai_phi ENUM('BAT_BUOC', 'TU_NGUYEN') NOT NULL,
    don_gia DECIMAL(15,2) DEFAULT 0,
    ngay_tao DATE NOT NULL,
    ghi_chu TEXT
) ENGINE=InnoDB;

-- 4. Table nhan_khau
CREATE TABLE IF NOT EXISTS nhan_khau (
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
CREATE TABLE IF NOT EXISTS tam_tru_tam_vang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nhan_khau_id INT NOT NULL,
    loai_khai_bao ENUM('TAM_TRU', 'TAM_VANG') NOT NULL,
    tu_ngay DATE NOT NULL,
    den_ngay DATE NOT NULL,
    ly_do TEXT,
    FOREIGN KEY (nhan_khau_id) REFERENCES nhan_khau(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Table nop_tien
CREATE TABLE IF NOT EXISTS nop_tien (
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

-- Truncate/Clear data before inserting to avoid duplicates during test runs (optional, but let's just insert with check or ignore if already exist)
-- For a fresh DB, let's insert sample data. We use INSERT IGNORE or handle it clean.
-- Since this is for initial setup:
INSERT IGNORE INTO users (id, username, password, role) VALUES 
(1, 'admin_blue', '123456', 'ADMIN'),
(2, 'ketoan_moon', '123456', 'ACCOUNTANT');

INSERT IGNORE INTO ho_khau (id, ma_ho_khau, ten_chu_ho, dien_tich, ngay_lap) VALUES 
(1, 'P101', 'Nguyễn Văn A', 75.50, '2023-01-15'),
(2, 'P102', 'Trần Thị B', 60.00, '2023-02-20');

INSERT IGNORE INTO nhan_khau (id, ho_khau_id, ho_ten, cccd, ngay_sinh, gioi_tinh, quan_he, trang_thai) VALUES 
(1, 1, 'Nguyễn Văn A', '001090123456', '1990-05-10', 'NAM', 'Chủ hộ', 'THUONG_TRU'),
(2, 1, 'Lê Thị C', '001092654321', '1992-08-15', 'NU', 'Vợ', 'THUONG_TRU'),
(3, 2, 'Trần Thị B', '001085789123', '1985-11-20', 'NU', 'Chủ hộ', 'THUONG_TRU');

INSERT IGNORE INTO khoan_thu (id, ma_khoan_thu, ten_khoan_thu, loai_phi, don_gia, ngay_tao, ghi_chu) VALUES 
(1, 'PDV_102023', 'Phí dịch vụ chung cư Tháng 10/2023', 'BAT_BUOC', 7000.00, '2023-10-01', '7.000đ/m2'),
(2, 'QTT_2023', 'Quỹ vì người nghèo năm 2023', 'TU_NGUYEN', 0, '2023-10-15', 'Tùy tâm cư dân');

INSERT IGNORE INTO nop_tien (id, ho_khau_id, khoan_thu_id, nguoi_nop, so_tien_nop, ngay_nop, hinh_thuc) VALUES 
(1, 1, 1, 'Nguyễn Văn A', 528500.00, '2023-10-05', 'CHUYEN_KHOAN'),
(2, 2, 2, 'Trần Thị B', 200000.00, '2023-10-16', 'TIEN_MAT');
