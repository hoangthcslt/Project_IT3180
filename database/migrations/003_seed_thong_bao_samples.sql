-- Add UI test samples without deleting or replacing existing notifications.
INSERT INTO thong_bao (ten_thong_bao, file_path, ngay_ban_hanh, trang_thai)
SELECT sample.title, NULL, sample.issue_date, sample.status
FROM (
    SELECT 'Thông báo họp cư dân tháng 7' title, '2026-07-01' issue_date, 'Đã phát hành' status UNION ALL
    SELECT 'Kế hoạch bảo trì thang máy', '2026-07-03', 'Đã phát hành' UNION ALL
    SELECT 'Thông báo cắt nước tạm thời', '2026-07-05', 'Đã phát hành' UNION ALL
    SELECT 'Quy định gửi xe mới', '2026-07-06', 'Đã phát hành' UNION ALL
    SELECT 'Báo cáo tài chính quý II', '2026-07-08', 'Đã phát hành' UNION ALL
    SELECT 'Hướng dẫn sử dụng phần mềm', '2026-07-10', 'Đã phát hành' UNION ALL
    SELECT 'Kiểm tra hệ thống PCCC', '2026-07-12', 'Đã phát hành' UNION ALL
    SELECT 'Thông báo vệ sinh khuôn viên', '2026-07-14', 'Đã phát hành' UNION ALL
    SELECT 'Họp Ban quản trị', '2026-07-16', 'Nháp' UNION ALL
    SELECT 'Nâng cấp hệ thống internet', '2026-07-18', 'Đã phát hành'
) sample
WHERE NOT EXISTS (
    SELECT 1 FROM thong_bao existing WHERE existing.ten_thong_bao = sample.title
);

-- Assign a suitable existing group without creating or replacing permission data.
INSERT IGNORE INTO thong_bao_group (thong_bao_id, group_id)
SELECT tb.id, ug.id
FROM thong_bao tb
JOIN user_group ug ON ug.ten_nhom IN ('Cư dân', 'RESIDENT')
WHERE tb.ten_thong_bao IN (
    'Thông báo họp cư dân tháng 7', 'Kế hoạch bảo trì thang máy', 'Thông báo cắt nước tạm thời',
    'Quy định gửi xe mới', 'Kiểm tra hệ thống PCCC', 'Thông báo vệ sinh khuôn viên',
    'Nâng cấp hệ thống internet'
);

INSERT IGNORE INTO thong_bao_group (thong_bao_id, group_id)
SELECT tb.id, ug.id FROM thong_bao tb JOIN user_group ug ON ug.ten_nhom IN ('Kế toán', 'ACCOUNTANT')
WHERE tb.ten_thong_bao = 'Báo cáo tài chính quý II';

INSERT IGNORE INTO thong_bao_group (thong_bao_id, group_id)
SELECT tb.id, ug.id FROM thong_bao tb JOIN user_group ug ON ug.ten_nhom IN ('Admin', 'ADMIN')
WHERE tb.ten_thong_bao IN ('Hướng dẫn sử dụng phần mềm', 'Họp Ban quản trị');
