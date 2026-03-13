-- ============================================================
-- SEED DATA for instructor@gmail.com (ID: 34278aec-...)
-- user_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
-- ============================================================

-- Dọn dữ liệu cũ của instructor này (nếu có)
DELETE FROM payments
  WHERE payment_notes LIKE '%[SEED-INS]%';

DELETE FROM enrollments
  WHERE course_id IN (
    SELECT course_id FROM courses WHERE course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
      AND course_title LIKE '%[SEED]%'
  );

DELETE FROM modules
  WHERE course_id IN (
    SELECT course_id FROM courses WHERE course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
  );

DELETE FROM courses
  WHERE course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
    AND course_title LIKE '%[SEED]%';

-- ============================================================
-- 1. COURSES cho instructor@gmail.com
-- ============================================================
INSERT INTO courses (course_id, course_constructor_id, course_title, course_description, course_price, course_status, course_jlpt_level, course_created_at)
VALUES
  ('aa000001-0000-0000-0000-000000000001', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'Hiragana & Katakana trong 7 ngày [SEED]', 'Học bảng chữ cái từ cơ bản.',
   199000, 'APPROVED', 'N5', NOW() - INTERVAL '5 months'),

  ('aa000001-0000-0000-0000-000000000002', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'JLPT N4 – Ngữ pháp toàn diện [SEED]', 'N4 ngữ pháp từ nền tảng đến nâng cao.',
   699000, 'APPROVED', 'N4', NOW() - INTERVAL '4 months'),

  ('aa000001-0000-0000-0000-000000000003', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'Tiếng Nhật cho người đi làm – N3 [SEED]', 'Giao tiếp thương mại N3.',
   799000, 'APPROVED', 'N3', NOW() - INTERVAL '3 months'),

  ('aa000001-0000-0000-0000-000000000004', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'JLPT N2 – Văn pháp nâng cao [SEED]', 'N2 chuyên sâu + đề thi thật.',
   999000, 'APPROVED', 'N2', NOW() - INTERVAL '2 months'),

  ('aa000001-0000-0000-0000-000000000005', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'Luyện nghe N3 thực chiến [SEED]', 'Kỹ năng nghe N3 qua tình huống thực tế.',
   549000, 'APPROVED', 'N3', NOW() - INTERVAL '2 months'),

  ('aa000001-0000-0000-0000-000000000006', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'Kaiwa – Hội thoại công sở N3 [SEED]', 'Hội thoại công việc bằng tiếng Nhật.',
   449000, 'PENDING_APPROVAL', 'N3', NOW() - INTERVAL '5 days'),

  ('aa000001-0000-0000-0000-000000000007', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'Kho Kanji N2 qua hình ảnh [SEED]', 'Kanji N2 học qua mnemonic và hình ảnh.',
   649000, 'PENDING_APPROVAL', 'N2', NOW() - INTERVAL '2 days'),

  ('aa000001-0000-0000-0000-000000000008', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'Chuẩn bị JLPT N1 – lộ trình 6 tháng [SEED]', 'N1 lộ trình chuẩn.',
   1299000, 'DRAFT', 'N1', NOW() - INTERVAL '1 day'),

  ('aa000001-0000-0000-0000-000000000009', '34278aec-a183-4c59-bc1f-c5efaef23d8a',
   'Anime & Manga – Học tiếng Nhật vui vẻ [SEED]', 'Học qua hoạt hình.',
   299000, 'REJECTED', 'N5', NOW() - INTERVAL '1 month')
ON CONFLICT (course_id) DO NOTHING;

-- ============================================================
-- 2. MODULES — >= 3 modules cho mỗi APPROVED/PENDING course
--    (Theo ErrorCode.MIN_MODULES_REQUIRED: "at least 3 modules")
-- ============================================================

-- Course 1: Hiragana & Katakana (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000001', 'Hiragana: 46 ký tự cơ bản',            1),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000001', 'Katakana: 46 ký tự ngoại lai',          2),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000001', 'Luyện viết và đọc tổng hợp',            3);

-- Course 2: N4 Ngữ pháp (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000002', 'Ngữ pháp N4 phần 1: Te-form nâng cao', 1),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000002', 'Ngữ pháp N4 phần 2: Cụm danh từ',      2),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000002', 'Luyện đề và bài kiểm tra N4',           3);

-- Course 3: N3 người đi làm (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000003', 'Hội thoại công sở cơ bản',              1),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000003', 'Email và văn bản thương mại',            2),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000003', 'Thuyết trình và họp hành N3',            3);

-- Course 4: N2 văn pháp (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000004', 'Ngữ pháp N2: Biểu hiện phức tạp',       1),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000004', 'Đọc hiểu N2 qua bài báo thực',           2),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000004', 'Đề thi N2 thực chiến (2020–2024)',        3);

-- Course 5: Luyện nghe N3 (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000005', 'Nghe N3: Hội thoại ngắn',               1),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000005', 'Nghe N3: Bản tin và thông báo',          2),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000005', 'Nghe N3: Đề thi thực chiến',             3);

-- Course 6: Kaiwa công sở (PENDING_APPROVAL) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000006', 'Tình huống công sở: Xin việc & phỏng vấn', 1),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000006', 'Hội thoại nhóm & cuộc họp',                2),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000006', 'Thực hành hội thoại qua video',             3);

-- Course 7: Kanji N2 (PENDING_APPROVAL) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000007', 'Kanji N2 nhóm 1: Ký tự thiên nhiên',    1),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000007', 'Kanji N2 nhóm 2: Ký tự xã hội',         2),
(gen_random_uuid(), 'aa000001-0000-0000-0000-000000000007', 'Ôn luyện Kanji N2 qua bài kiểm tra',    3);

-- Course 8: DRAFT — không cần module
-- Course 9: REJECTED — không cần module

-- ============================================================
-- 2. STUDENTS để ghi danh (dùng lại từ seed_data cũ)
--    Đảm bảo các user này tồn tại
-- ============================================================
INSERT INTO users (user_id, full_name, email, password_hash, role, enabled, notes, created_at) VALUES
('00000000-0000-0002-0000-000000000020', 'Nguyễn Thị Lan',  'lan.nguyen.seed@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '5 months'),
('00000000-0000-0002-0000-000000000021', 'Trần Văn Nam',    'nam.tran.seed@yahoo.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '5 months'),
('00000000-0000-0002-0000-000000000022', 'Lê Minh Khôi',    'khoi.le.seed@outlook.com',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '4 months'),
('00000000-0000-0002-0000-000000000023', 'Phạm Thu Hà',     'ha.pham.seed@fpt.edu.vn',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '4 months'),
('00000000-0000-0002-0000-000000000024', 'Hoàng Đức Anh',   'ducanh.h.seed@gmail.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '3 months'),
('00000000-0000-0002-0000-000000000025', 'Vũ Thị Mai',      'mai.vu.seed@gmail.com',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '3 months'),
('00000000-0000-0002-0000-000000000026', 'Đặng Quốc Hùng',  'hung.d.seed@gmail.com',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '2 months'),
('00000000-0000-0002-0000-000000000028', 'Ngô Thanh Bình',  'binh.ngo.seed@gmail.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '1 month'),
('00000000-0000-0002-0000-000000000029', 'Trương Minh Tuấn','tuan.t.seed@gmail.com',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true, '[SEED]', NOW()-INTERVAL '3 weeks')
ON CONFLICT (user_id) DO NOTHING;

-- ============================================================
-- 3. ENROLLMENTS — vào các khóa APPROVED của instructor này
-- ============================================================
INSERT INTO enrollments (enrollments_id, user_id, course_id, enrollments_enrolled_at, status) VALUES
-- Course 1: N5 Hiragana (5 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 'aa000001-0000-0000-0000-000000000001', NOW()-INTERVAL '5 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000021', 'aa000001-0000-0000-0000-000000000001', NOW()-INTERVAL '4 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000022', 'aa000001-0000-0000-0000-000000000001', NOW()-INTERVAL '3 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000028', 'aa000001-0000-0000-0000-000000000001', NOW()-INTERVAL '1 month',  'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000029', 'aa000001-0000-0000-0000-000000000001', NOW()-INTERVAL '2 weeks',  'ACTIVE'),

-- Course 2: N4 Ngữ pháp (4 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 'aa000001-0000-0000-0000-000000000002', NOW()-INTERVAL '4 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 'aa000001-0000-0000-0000-000000000002', NOW()-INTERVAL '3 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000024', 'aa000001-0000-0000-0000-000000000002', NOW()-INTERVAL '2 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000026', 'aa000001-0000-0000-0000-000000000002', NOW()-INTERVAL '1 month',  'ACTIVE'),

-- Course 3: N3 người đi làm (3 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000022', 'aa000001-0000-0000-0000-000000000003', NOW()-INTERVAL '3 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000025', 'aa000001-0000-0000-0000-000000000003', NOW()-INTERVAL '2 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000026', 'aa000001-0000-0000-0000-000000000003', NOW()-INTERVAL '1 month',  'ACTIVE'),

-- Course 4: N2 văn pháp (2 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 'aa000001-0000-0000-0000-000000000004', NOW()-INTERVAL '2 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000024', 'aa000001-0000-0000-0000-000000000004', NOW()-INTERVAL '1 month',  'ACTIVE'),

-- Course 5: N3 luyện nghe (3 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000021', 'aa000001-0000-0000-0000-000000000005', NOW()-INTERVAL '2 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000028', 'aa000001-0000-0000-0000-000000000005', NOW()-INTERVAL '1 month',  'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000029', 'aa000001-0000-0000-0000-000000000005', NOW()-INTERVAL '2 weeks',  'ACTIVE')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 4. PAYMENTS — rải đều 6 tháng (linked đến enrollments)
--    Dùng payment_notes = '[SEED-INS]' để phân biệt
-- ============================================================
INSERT INTO payments (payment_id, user_id, payment_amount, payment_status, payment_method, payment_transaction_id, payment_created_at, payment_completed_at, payment_notes, enrollment_id)
SELECT gen_random_uuid(), e.user_id, c.course_price, 'COMPLETED',
       CASE WHEN random() > 0.5 THEN 'VNPAY' ELSE 'MOMO' END,
       'INS-' || substring(e.enrollments_id::text, 1, 8),
       e.enrollments_enrolled_at,
       e.enrollments_enrolled_at,
       '[SEED-INS]',
       e.enrollments_id
FROM enrollments e
JOIN courses c ON e.course_id = c.course_id
WHERE c.course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
ON CONFLICT (payment_transaction_id) DO NOTHING;

-- ============================================================
-- 5. Kiểm tra
-- ============================================================
SELECT '=== Instructor Dashboard Seed ===' AS info;
SELECT 'courses'     AS tbl, COUNT(*) FROM courses  WHERE course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments WHERE course_id::text LIKE 'aa000001%'
UNION ALL
SELECT 'payments',    COUNT(*) FROM payments WHERE payment_notes = '[SEED-INS]';

SELECT
  c.course_title,
  c.course_status,
  c.course_price,
  COUNT(e.enrollments_id) AS so_hoc_vien,
  SUM(p.payment_amount)   AS doanh_thu
FROM courses c
LEFT JOIN enrollments e ON e.course_id = c.course_id
LEFT JOIN payments p    ON p.enrollment_id = e.enrollments_id AND p.payment_status = 'COMPLETED'
WHERE c.course_constructor_id = '34278aec-a183-4c59-bc1f-c5efaef23d8a'
GROUP BY c.course_id, c.course_title, c.course_status, c.course_price
ORDER BY so_hoc_vien DESC;
