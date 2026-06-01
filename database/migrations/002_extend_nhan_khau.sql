-- Extend the existing table in place. This migration never drops or recreates nhan_khau.
-- It is safe to run more than once.
DROP PROCEDURE IF EXISTS extend_nhan_khau;
DELIMITER //
CREATE PROCEDURE extend_nhan_khau()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nhan_khau' AND COLUMN_NAME = 'so_dien_thoai'
    ) THEN
        ALTER TABLE nhan_khau
            ADD COLUMN so_dien_thoai VARCHAR(15) NULL AFTER cccd;
    END IF;
END//
DELIMITER ;

CALL extend_nhan_khau();
DROP PROCEDURE extend_nhan_khau;
