-- Extend the existing table in place. This migration never drops or recreates ho_khau.
-- It is safe to run more than once.
DROP PROCEDURE IF EXISTS extend_ho_khau;
DELIMITER //
CREATE PROCEDURE extend_ho_khau()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ho_khau' AND COLUMN_NAME = 'status'
    ) THEN
        ALTER TABLE ho_khau
            ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'Đang ở' AFTER dien_tich;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ho_khau' AND COLUMN_NAME = 'so_nguoi'
    ) THEN
        ALTER TABLE ho_khau
            ADD COLUMN so_nguoi INT NOT NULL DEFAULT 0 AFTER status;

        UPDATE ho_khau hk
        LEFT JOIN (
            SELECT ho_khau_id, COUNT(*) AS total
            FROM nhan_khau
            GROUP BY ho_khau_id
        ) nk ON nk.ho_khau_id = hk.id
        SET hk.so_nguoi = COALESCE(nk.total, 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ho_khau' AND COLUMN_NAME = 'phuong_tien'
    ) THEN
        ALTER TABLE ho_khau
            ADD COLUMN phuong_tien VARCHAR(255) NOT NULL DEFAULT 'Chưa cập nhật' AFTER so_nguoi;
    END IF;
END//
DELIMITER ;

CALL extend_ho_khau();
DROP PROCEDURE extend_ho_khau;
