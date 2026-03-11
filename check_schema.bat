-- ============================================================
-- FIX SEED: Đổi APPROVED → DRAFT cho các khóa seed
-- (vì chúng không có modules nên không hợp lệ để APPROVED)
-- Sau đó tạo modules cho từng khóa, submit-approval → PENDING
-- ============================================================

-- 1. Đổi tất cả APPROVED seed courses → DRAFT 
UPDATE courses
SET course_status = 'DRAFT'
WHERE course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
  AND course_title LIKE '%[SEED]%';

-- 2. Thêm modules cho 5 khóa học chính
--    Module table: cần xem schema thực tế
-- SELECT column_name FROM information_schema.columns WHERE table_name = 'modules';

-- Chạy lệnh này để kiểm tra schema modules:
\d modules;

-- 3. Kiểm tra kết quả
SELECT course_title, course_status
FROM courses
WHERE course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
ORDER BY course_created_at;
