-- Script cập nhật Database cho hệ thống BlueMoon
-- Hãy chạy script này trong MySQL Workbench hoặc phpMyAdmin (sử dụng database: bluemoon_db)

USE `bluemoon_db`;

-- 1. Tạo bảng danh_muc_phi (Danh mục phí cố định)
CREATE TABLE IF NOT EXISTS `danh_muc_phi` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `ma_phi` VARCHAR(20) NOT NULL UNIQUE,
  `ten_phi` VARCHAR(100) NOT NULL,
  `loai_phi` ENUM('BAT_BUOC', 'TU_NGUYEN') NOT NULL DEFAULT 'BAT_BUOC',
  `loai_tinh_gia` ENUM('CO_DINH', 'THEO_DIEN_TICH', 'THEO_SO_NGUOI', 'NHAP_TAY') NOT NULL,
  `don_gia` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `ghi_chu` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. Thêm dữ liệu mặc định cho danh_muc_phi
INSERT IGNORE INTO `danh_muc_phi` (`ma_phi`, `ten_phi`, `loai_phi`, `loai_tinh_gia`, `don_gia`, `ghi_chu`) VALUES 
('DIEN', 'Tiền Điện', 'BAT_BUOC', 'NHAP_TAY', 2500.00, 'Đơn giá điện: 2500đ/kWh'),
('NUOC', 'Tiền Nước', 'BAT_BUOC', 'NHAP_TAY', 10000.00, 'Đơn giá nước: 10000đ/m3'),
('INTERNET', 'Tiền Internet', 'BAT_BUOC', 'CO_DINH', 250000.00, 'Trọn gói hàng tháng'),
('PHISV', 'Phí Dịch Vụ', 'BAT_BUOC', 'THEO_DIEN_TICH', 7000.00, 'Phí dịch vụ chung cư: 7000đ/m2/tháng'),
('PHISQL', 'Phí Quản Lý', 'BAT_BUOC', 'THEO_DIEN_TICH', 7000.00, 'Phí quản lý: 7000đ/m2/tháng'),
('MOITRUONG', 'Phí Môi Trường', 'BAT_BUOC', 'THEO_SO_NGUOI', 6000.00, 'Phí môi trường/vệ sinh: 6000đ/người/tháng'),
('ANNINH', 'Phí An Ninh', 'BAT_BUOC', 'THEO_DIEN_TICH', 5000.00, 'Phí bảo đảm an ninh: 5000đ/m2/tháng'),
('QUYTUTHEN', 'Quỹ Từ Thiện', 'TU_NGUYEN', 'NHAP_TAY', 0.00, 'Quyên góp tự nguyện');

-- 3. Cập nhật bảng ho_khau (thêm số lượng xe)
DROP PROCEDURE IF EXISTS AddColumnIfNotExist;
DELIMITER //
CREATE PROCEDURE AddColumnIfNotExist(
    IN dbName VARCHAR(64),
    IN tableName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN columnDDL VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = dbName 
          AND TABLE_NAME = tableName 
          AND COLUMN_NAME = columnName
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', tableName, '` ADD COLUMN `', columnName, '` ', columnDDL);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddColumnIfNotExist('bluemoon_db', 'ho_khau', 'so_xe_may', 'INT NOT NULL DEFAULT 0');
CALL AddColumnIfNotExist('bluemoon_db', 'ho_khau', 'so_oto', 'INT NOT NULL DEFAULT 0');

-- 4. Cập nhật bảng khoan_thu (thêm hạn nộp và trạng thái đợt thu)
CALL AddColumnIfNotExist('bluemoon_db', 'khoan_thu', 'han_nop', 'DATE');
CALL AddColumnIfNotExist('bluemoon_db', 'khoan_thu', 'trang_thai', 'VARCHAR(20) DEFAULT \'DRAFT\'');

-- 5. Tạo bảng hoa_don (Hóa đơn tổng cho từng hộ theo đợt thu)
CREATE TABLE IF NOT EXISTS `hoa_don` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `khoan_thu_id` INT NOT NULL,
  `ho_khau_id` INT NOT NULL,
  `ma_hoa_don` VARCHAR(30) NOT NULL UNIQUE,
  `tong_tien` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `so_tien_da_nop` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `trang_thai` ENUM('CHUA_NOP', 'DA_NOP') NOT NULL DEFAULT 'CHUA_NOP',
  `ngay_tao` DATE NOT NULL,
  `han_nop` DATE NOT NULL,
  FOREIGN KEY (`khoan_thu_id`) REFERENCES `khoan_thu`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`ho_khau_id`) REFERENCES `ho_khau`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 6. Tạo bảng chi_tiet_hoa_don (Chi tiết từng dịch vụ trong hóa đơn)
CREATE TABLE IF NOT EXISTS `chi_tiet_hoa_don` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `hoa_don_id` INT NOT NULL,
  `ma_phi` VARCHAR(20) NOT NULL,
  `ten_phi` VARCHAR(100) NOT NULL,
  `don_gia` DECIMAL(15,2) NOT NULL,
  `so_luong` DECIMAL(10,2) NOT NULL,
  `thanh_tien` DECIMAL(15,2) NOT NULL,
  FOREIGN KEY (`hoa_don_id`) REFERENCES `hoa_don`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 7. Cập nhật bảng nop_tien để liên kết với bảng hoa_don
CALL AddColumnIfNotExist('bluemoon_db', 'nop_tien', 'hoa_don_id', 'INT DEFAULT NULL');

-- Thêm Foreign Key nếu chưa có
DROP PROCEDURE IF EXISTS AddForeignKeyIfNotExist;
DELIMITER //
CREATE PROCEDURE AddForeignKeyIfNotExist(
    IN dbName VARCHAR(64),
    IN tableName VARCHAR(64),
    IN constraintName VARCHAR(64),
    IN fkDDL VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.TABLE_CONSTRAINTS 
        WHERE CONSTRAINT_SCHEMA = dbName 
          AND TABLE_NAME = tableName 
          AND CONSTRAINT_NAME = constraintName
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', tableName, '` ADD CONSTRAINT `', constraintName, '` ', fkDDL);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddForeignKeyIfNotExist('bluemoon_db', 'nop_tien', 'fk_nop_tien_hoa_don', 'FOREIGN KEY (`hoa_don_id`) REFERENCES `hoa_don`(`id`) ON DELETE CASCADE');

DROP PROCEDURE IF EXISTS AddColumnIfNotExist;
DROP PROCEDURE IF EXISTS AddForeignKeyIfNotExist;

-- 8. Tạo các bảng phục vụ chức năng thông báo (Tiện ích)
CREATE TABLE IF NOT EXISTS `thong_bao` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `ten_thong_bao` VARCHAR(255) NOT NULL,
  `file_path` VARCHAR(255) DEFAULT NULL,
  `ngay_ban_hanh` DATE NOT NULL,
  `trang_thai` VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `thong_bao_group` (
  `thong_bao_id` INT NOT NULL,
  `group_id` INT NOT NULL,
  PRIMARY KEY (`thong_bao_id`, `group_id`),
  FOREIGN KEY (`thong_bao_id`) REFERENCES `thong_bao`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`group_id`) REFERENCES `user_group`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `user_read_notification` (
  `user_id` INT NOT NULL,
  `thong_bao_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `thong_bao_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`thong_bao_id`) REFERENCES `thong_bao`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 9. Sửa cột role trong bảng users để hỗ trợ các nhóm phân quyền linh hoạt
ALTER TABLE `users` MODIFY COLUMN `role` VARCHAR(100) NOT NULL;
