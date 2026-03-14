-- ============================================================
-- seed_modules.sql — ĐÃ TÍCH HỢP VÀO seed_data.sql & seed_instructor.sql
-- ============================================================
-- Modules cho courses SEED admin (bb000001-...) đã được thêm trực tiếp
-- vào seed_data.sql (phần 3. MODULES).
--
-- Modules cho courses của instructor@gmail.com (aa000001-...) đã được
-- thêm trực tiếp vào seed_instructor.sql (phần 2. MODULES).
--
-- File này chỉ giữ lại để kiểm tra nhanh module count toàn bộ:
-- ============================================================

SELECT
    c.course_title,
    c.course_status,
    COUNT(m.modules_id) AS module_count,
    CASE
        WHEN c.course_status IN ('APPROVED', 'PENDING_APPROVAL') AND COUNT(m.modules_id) >= 3
            THEN '✓ OK (>= 3 modules)'
        WHEN c.course_status IN ('APPROVED', 'PENDING_APPROVAL') AND COUNT(m.modules_id) < 3
            THEN '✗ THIẾU MODULE!'
        ELSE '— (DRAFT/REJECTED, không yêu cầu)'
    END AS kiem_tra
FROM courses c
LEFT JOIN modules m ON m.course_id = c.course_id
WHERE c.course_constructor_id LIKE '00000000-0000-0001%'
   OR c.course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
GROUP BY c.course_id, c.course_title, c.course_status
ORDER BY c.course_status, c.course_created_at;
