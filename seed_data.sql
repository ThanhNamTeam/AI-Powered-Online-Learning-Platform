-- ============================================================
-- SEED DATA for SWD392 Admin Dashboard
-- DB: swd392_db | Chạy: check_schema.bat hoặc run_seed.bat
-- ============================================================
-- Lưu ý:
--   - APPROVED courses phải có >= 3 modules (theo ErrorCode.MIN_MODULES_REQUIRED)
--   - Dùng fixed UUID cho courses để modules có thể insert chính xác
-- ============================================================

-- Dọn dữ liệu SEED cũ (theo đúng thứ tự FK)
-- 1. Payments trước (FK → enrollments)
DELETE FROM payments WHERE payment_notes LIKE '%[SEED]%' OR payment_notes LIKE '%[SEED-INS]%';
-- 2. Enrollments (FK → users, courses)
DELETE FROM enrollments
  WHERE user_id::text LIKE '00000000-0000-0002%'
     OR course_id IN (SELECT course_id FROM courses WHERE course_id::text LIKE 'bb000001%');
-- 3. Modules (FK → courses)
DELETE FROM modules WHERE course_id::text LIKE 'bb000001%';
-- 4. Courses
DELETE FROM courses WHERE course_id::text LIKE 'bb000001%';
-- 5. Users SEED (chỉ xóa sau khi không còn FK nào trỏ vào)
DELETE FROM users WHERE notes LIKE '%[SEED]%' AND user_id::text LIKE '00000000-0000-0001%' OR notes LIKE '%[SEED]%' AND user_id::text LIKE '00000000-0000-0002%';

-- ============================================================
-- 1. USERS
-- password = BCrypt("Password123@")
-- ============================================================
INSERT INTO users (user_id, full_name, email, password_hash, role, enabled, notes, created_at) VALUES

-- INSTRUCTORS (4 người)
('00000000-0000-0001-0000-000000000010', 'Yamada Akira',       'yamada.akira@jp.com',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'INSTRUCTOR', true,  '[SEED]', NOW() - INTERVAL '5 months'),
('00000000-0000-0001-0000-000000000011', 'Nguyễn Thanh Hương', 'huong.nguyen.seed@gm.com',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'INSTRUCTOR', true,  '[SEED]', NOW() - INTERVAL '4 months'),
('00000000-0000-0001-0000-000000000012', 'Sato Kenji',         'sato.kenji.seed@jp123.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'INSTRUCTOR', true,  '[SEED]', NOW() - INTERVAL '3 months'),
('00000000-0000-0001-0000-000000000013', 'Tanaka Yuki',        'tanaka.yuki.seed@jp.co',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'INSTRUCTOR', false, '[SEED]', NOW() - INTERVAL '2 months'),

-- STUDENTS (10 người)
('00000000-0000-0002-0000-000000000020', 'Nguyễn Thị Lan',   'lan.nguyen.seed@gmail.com',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '5 months'),
('00000000-0000-0002-0000-000000000021', 'Trần Văn Nam',      'nam.tran.seed@yahoo.com',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '5 months'),
('00000000-0000-0002-0000-000000000022', 'Lê Minh Khôi',      'khoi.le.seed@outlook.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '4 months'),
('00000000-0000-0002-0000-000000000023', 'Phạm Thu Hà',       'ha.pham.seed@fpt.edu.vn',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '4 months'),
('00000000-0000-0002-0000-000000000024', 'Hoàng Đức Anh',     'ducanh.h.seed@gmail.com',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '3 months'),
('00000000-0000-0002-0000-000000000025', 'Vũ Thị Mai',        'mai.vu.seed@gmail.com',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '3 months'),
('00000000-0000-0002-0000-000000000026', 'Đặng Quốc Hùng',   'hung.d.seed@gmail.com',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '2 months'),
('00000000-0000-0002-0000-000000000027', 'Bùi Cẩm Tú',       'camtu.b.seed@gmail.com',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', false, '[SEED]', NOW() - INTERVAL '2 months'),
('00000000-0000-0002-0000-000000000028', 'Ngô Thanh Bình',   'binh.ngo.seed@gmail.com',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '1 month'),
('00000000-0000-0002-0000-000000000029', 'Trương Minh Tuấn', 'tuan.t.seed@gmail.com',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'STUDENT', true,  '[SEED]', NOW() - INTERVAL '3 weeks')

ON CONFLICT (user_id) DO NOTHING;

-- ============================================================
-- 2. COURSES — dùng fixed UUID để modules có thể match chính xác
--    APPROVED courses (bb000001-...) cần >= 3 modules
-- ============================================================
INSERT INTO courses (course_id, course_constructor_id, course_title, course_description, course_price, course_status, course_jlpt_level, course_created_at) VALUES

-- APPROVED (7 khóa — mỗi khóa sẽ có >= 3 modules bên dưới)
('bb000001-0000-0000-0000-000000000001', '00000000-0000-0001-0000-000000000010',
 'Hiragana & Katakana trong 7 ngày',           'Học bảng chữ cái Nhật Bản từ cơ bản.',              199000,  'APPROVED',         'N5', NOW() - INTERVAL '5 months'),

('bb000001-0000-0000-0000-000000000002', '00000000-0000-0001-0000-000000000010',
 'Minna no Nihongo I – N5 Cấp tốc',            'Khóa học N5 toàn diện theo giáo trình.',             499000,  'APPROVED',         'N5', NOW() - INTERVAL '4 months'),

('bb000001-0000-0000-0000-000000000003', '00000000-0000-0001-0000-000000000011',
 'Luyện thi JLPT N4 – Ngữ pháp & Đọc hiểu',   'N4 ngữ pháp bài bản từ cơ bản đến nâng cao.',       699000,  'APPROVED',         'N4', NOW() - INTERVAL '4 months'),

('bb000001-0000-0000-0000-000000000004', '00000000-0000-0001-0000-000000000011',
 'Tiếng Nhật cho người đi làm – N3 Toàn diện', 'Giao tiếp thương mại N3.',                          799000,  'APPROVED',         'N3', NOW() - INTERVAL '3 months'),

('bb000001-0000-0000-0000-000000000005', '00000000-0000-0001-0000-000000000010',
 'JLPT N2 – Văn pháp nâng cao + Đề thi thật',  'N2 chuyên sâu + đề thi thật.',                     999000,  'APPROVED',         'N2', NOW() - INTERVAL '3 months'),

('bb000001-0000-0000-0000-000000000006', '00000000-0000-0001-0000-000000000013',
 'JLPT N1 – Chinh phục kỳ thi khó nhất',       'N1 toàn diện từ ngữ pháp đến đọc hiểu.',           1299000, 'APPROVED',         'N1', NOW() - INTERVAL '2 months'),

('bb000001-0000-0000-0000-000000000007', '00000000-0000-0001-0000-000000000011',
 'Tiếng Nhật thương mại – Business Japanese',   'Kỹ năng giao tiếp công sở Nhật Bản.',               899000,  'APPROVED',         'N3', NOW() - INTERVAL '2 months'),

-- PENDING_APPROVAL (3 khóa — có >= 3 modules để hợp lệ khi PENDING)
('bb000001-0000-0000-0000-000000000008', '00000000-0000-0001-0000-000000000010',
 'Kaiwa thực chiến: Giao tiếp công sở',         'Lớp hội thoại thực tế với tình huống công việc.',   399000,  'PENDING_APPROVAL', 'N3', NOW() - INTERVAL '10 days'),

('bb000001-0000-0000-0000-000000000009', '00000000-0000-0001-0000-000000000011',
 'Hán tự (Kanji) N3 qua hình ảnh',              'Phương pháp ghi nhớ Kanji bằng hình ảnh.',          549000,  'PENDING_APPROVAL', 'N3', NOW() - INTERVAL '7 days'),

('bb000001-0000-0000-0000-000000000010', '00000000-0000-0001-0000-000000000012',
 'Luyện nghe JLPT N2 – Audio nâng cao',         'Tài liệu nghe N2 chuyên sâu.',                      749000,  'PENDING_APPROVAL', 'N2', NOW() - INTERVAL '5 days'),

-- REJECTED (1 khóa)
('bb000001-0000-0000-0000-000000000011', '00000000-0000-0001-0000-000000000012',
 'Anime & Manga – Học tiếng Nhật qua hoạt hình','Học qua phim hoạt hình Nhật Bản.',                  299000,  'REJECTED',         'N5', NOW() - INTERVAL '1 month'),

-- DRAFT (1 khóa)
('bb000001-0000-0000-0000-000000000012', '00000000-0000-0001-0000-000000000013',
 'Chuẩn bị du học Nhật – JLPT N4',             'Khóa học chuẩn bị du học toàn diện.',               599000,  'DRAFT',            'N4', NOW() - INTERVAL '3 days')

ON CONFLICT (course_id) DO NOTHING;

-- ============================================================
-- 3. MODULES — bắt buộc >= 3 module cho mỗi APPROVED/PENDING course
--    (Backend ErrorCode.MIN_MODULES_REQUIRED: "at least 3 modules")
-- ============================================================

-- Course 1: Hiragana & Katakana (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000001', 'Hiragana: 46 ký tự cơ bản',            1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000001', 'Katakana: 46 ký tự ngoại lai',          2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000001', 'Luyện viết và đọc tổng hợp',            3);

-- Course 2: Minna no Nihongo N5 (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000002', 'Bài 1–6: Giới thiệu bản thân & số đếm', 1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000002', 'Bài 7–13: Hoạt động hàng ngày',         2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000002', 'Bài 14–20: Ôn tập & kiểm tra tổng hợp', 3);

-- Course 3: JLPT N4 Ngữ pháp (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000003', 'Ngữ pháp N4 phần 1: Te-form nâng cao',  1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000003', 'Ngữ pháp N4 phần 2: Cụm danh từ',       2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000003', 'Luyện đề và bài kiểm tra N4',            3);

-- Course 4: Tiếng Nhật người đi làm N3 (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000004', 'Hội thoại công sở cơ bản',              1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000004', 'Email và văn bản thương mại',            2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000004', 'Thuyết trình và họp hành N3',            3);

-- Course 5: JLPT N2 Văn pháp (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000005', 'Ngữ pháp N2: Biểu hiện phức tạp',       1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000005', 'Đọc hiểu N2 qua bài báo thực',           2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000005', 'Đề thi N2 thực chiến (2020–2024)',        3);

-- Course 6: JLPT N1 (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000006', 'Từ vựng & Kanji N1 chuyên sâu',          1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000006', 'Ngữ pháp N1: Cấu trúc phức tạp',         2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000006', 'Đọc hiểu & Nghe N1 thực chiến',           3);

-- Course 7: Business Japanese (APPROVED) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000007', 'Keigo – Kính ngữ công sở',               1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000007', 'Giao tiếp điện thoại & Email doanh nghiệp',2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000007', 'Họp hành & Báo cáo bằng tiếng Nhật',     3);

-- Course 8: Kaiwa công sở (PENDING_APPROVAL) — 3 modules (hợp lệ để submit)
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000008', 'Tình huống công sở: Xin việc & phỏng vấn',1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000008', 'Hội thoại nhóm & cuộc họp',               2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000008', 'Thực hành hội thoại qua video tình huống', 3);

-- Course 9: Kanji N3 (PENDING_APPROVAL) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000009', 'Kanji N3 nhóm 1: Thiên nhiên & con người', 1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000009', 'Kanji N3 nhóm 2: Xã hội & hoạt động',     2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000009', 'Ôn luyện Kanji N3 qua bài kiểm tra',       3);

-- Course 10: Luyện nghe N2 (PENDING_APPROVAL) — 3 modules
INSERT INTO modules (modules_id, course_id, modules_title, modules_order_index) VALUES
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000010', 'Nghe N2: Hội thoại ngắn & điền từ',        1),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000010', 'Nghe N2: Bản tin & thông báo chính thức',  2),
(gen_random_uuid(), 'bb000001-0000-0000-0000-000000000010', 'Nghe N2: Đề thi thực chiến 2020–2024',     3);

-- Course 11: Anime & Manga (REJECTED) — không cần module (đã bị từ chối)
-- Course 12: Chuẩn bị du học (DRAFT) — không cần module (còn nháp)

-- ============================================================
-- 4. ENROLLMENTS — vào các APPROVED courses
-- ============================================================
INSERT INTO enrollments (enrollments_id, user_id, course_id, enrollments_enrolled_at, status) VALUES

-- Course 1: Hiragana (4 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 'bb000001-0000-0000-0000-000000000001', NOW()-INTERVAL '5 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000021', 'bb000001-0000-0000-0000-000000000001', NOW()-INTERVAL '4 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 'bb000001-0000-0000-0000-000000000001', NOW()-INTERVAL '3 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000029', 'bb000001-0000-0000-0000-000000000001', NOW()-INTERVAL '2 weeks',  'ACTIVE'),

-- Course 2: Minna N5 (4 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 'bb000001-0000-0000-0000-000000000002', NOW()-INTERVAL '4 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000021', 'bb000001-0000-0000-0000-000000000002', NOW()-INTERVAL '4 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000024', 'bb000001-0000-0000-0000-000000000002', NOW()-INTERVAL '3 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000028', 'bb000001-0000-0000-0000-000000000002', NOW()-INTERVAL '1 month',  'ACTIVE'),

-- Course 3: N4 Ngữ pháp (3 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 'bb000001-0000-0000-0000-000000000003', NOW()-INTERVAL '3 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000022', 'bb000001-0000-0000-0000-000000000003', NOW()-INTERVAL '3 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000025', 'bb000001-0000-0000-0000-000000000003', NOW()-INTERVAL '2 months', 'ACTIVE'),

-- Course 4: N3 người đi làm (2 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000022', 'bb000001-0000-0000-0000-000000000004', NOW()-INTERVAL '2 months', 'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 'bb000001-0000-0000-0000-000000000004', NOW()-INTERVAL '2 months', 'ACTIVE'),

-- Course 5: N2 văn pháp (3 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 'bb000001-0000-0000-0000-000000000005', NOW()-INTERVAL '1 month',  'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000024', 'bb000001-0000-0000-0000-000000000005', NOW()-INTERVAL '1 month',  'ACTIVE'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000028', 'bb000001-0000-0000-0000-000000000005', NOW()-INTERVAL '2 weeks',  'ACTIVE'),

-- Course 6: N1 (1 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000025', 'bb000001-0000-0000-0000-000000000006', NOW()-INTERVAL '1 month', 'ACTIVE'),

-- Course 7: Business Japanese (1 học viên)
(gen_random_uuid(), '00000000-0000-0002-0000-000000000026', 'bb000001-0000-0000-0000-000000000007', NOW()-INTERVAL '1 month', 'ACTIVE')

ON CONFLICT DO NOTHING;

-- ============================================================
-- 5. PAYMENTS — rải đều để biểu đồ doanh thu có dữ liệu 6 tháng
-- ============================================================
INSERT INTO payments (payment_id, user_id, payment_amount, payment_status, payment_method, payment_transaction_id, payment_created_at, payment_completed_at, payment_notes)
VALUES
-- Tháng -5
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 199000,  'COMPLETED', 'VNPAY', 'SEED-001', NOW()-INTERVAL '5 months'-INTERVAL '5 days',  NOW()-INTERVAL '5 months'-INTERVAL '5 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000021', 199000,  'COMPLETED', 'MOMO',  'SEED-002', NOW()-INTERVAL '5 months'-INTERVAL '3 days',  NOW()-INTERVAL '5 months'-INTERVAL '3 days',  '[SEED]'),

-- Tháng -4
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 499000,  'COMPLETED', 'VNPAY', 'SEED-003', NOW()-INTERVAL '4 months'-INTERVAL '10 days', NOW()-INTERVAL '4 months'-INTERVAL '10 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000021', 499000,  'COMPLETED', 'MOMO',  'SEED-004', NOW()-INTERVAL '4 months'-INTERVAL '7 days',  NOW()-INTERVAL '4 months'-INTERVAL '7 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000022', 699000,  'COMPLETED', 'VNPAY', 'SEED-005', NOW()-INTERVAL '4 months'-INTERVAL '5 days',  NOW()-INTERVAL '4 months'-INTERVAL '5 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 199000,  'COMPLETED', 'MOMO',  'SEED-006', NOW()-INTERVAL '4 months'-INTERVAL '2 days',  NOW()-INTERVAL '4 months'-INTERVAL '2 days',  '[SEED]'),

-- Tháng -3
(gen_random_uuid(), '00000000-0000-0002-0000-000000000020', 699000,  'COMPLETED', 'VNPAY', 'SEED-007', NOW()-INTERVAL '3 months'-INTERVAL '15 days', NOW()-INTERVAL '3 months'-INTERVAL '15 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000022', 799000,  'COMPLETED', 'MOMO',  'SEED-008', NOW()-INTERVAL '3 months'-INTERVAL '12 days', NOW()-INTERVAL '3 months'-INTERVAL '12 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 799000,  'COMPLETED', 'VNPAY', 'SEED-009', NOW()-INTERVAL '3 months'-INTERVAL '8 days',  NOW()-INTERVAL '3 months'-INTERVAL '8 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000024', 499000,  'COMPLETED', 'MOMO',  'SEED-010', NOW()-INTERVAL '3 months'-INTERVAL '5 days',  NOW()-INTERVAL '3 months'-INTERVAL '5 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000025', 699000,  'COMPLETED', 'VNPAY', 'SEED-011', NOW()-INTERVAL '3 months'-INTERVAL '1 day',   NOW()-INTERVAL '3 months'-INTERVAL '1 day',   '[SEED]'),

-- Tháng -2
(gen_random_uuid(), '00000000-0000-0002-0000-000000000023', 999000,  'COMPLETED', 'VNPAY', 'SEED-012', NOW()-INTERVAL '2 months'-INTERVAL '20 days', NOW()-INTERVAL '2 months'-INTERVAL '20 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000024', 999000,  'COMPLETED', 'MOMO',  'SEED-013', NOW()-INTERVAL '2 months'-INTERVAL '15 days', NOW()-INTERVAL '2 months'-INTERVAL '15 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000025', 1299000, 'COMPLETED', 'VNPAY', 'SEED-014', NOW()-INTERVAL '2 months'-INTERVAL '10 days', NOW()-INTERVAL '2 months'-INTERVAL '10 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000026', 899000,  'COMPLETED', 'MOMO',  'SEED-015', NOW()-INTERVAL '2 months'-INTERVAL '5 days',  NOW()-INTERVAL '2 months'-INTERVAL '5 days',  '[SEED]'),

-- Tháng -1
(gen_random_uuid(), '00000000-0000-0002-0000-000000000026', 899000,  'COMPLETED', 'VNPAY', 'SEED-016', NOW()-INTERVAL '1 month'-INTERVAL '20 days',  NOW()-INTERVAL '1 month'-INTERVAL '20 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000025', 1299000, 'COMPLETED', 'MOMO',  'SEED-017', NOW()-INTERVAL '1 month'-INTERVAL '15 days',  NOW()-INTERVAL '1 month'-INTERVAL '15 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000028', 499000,  'COMPLETED', 'MOMO',  'SEED-018', NOW()-INTERVAL '1 month'-INTERVAL '10 days',  NOW()-INTERVAL '1 month'-INTERVAL '10 days',  '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000029', 199000,  'COMPLETED', 'VNPAY', 'SEED-019', NOW()-INTERVAL '1 month'-INTERVAL '5 days',   NOW()-INTERVAL '1 month'-INTERVAL '5 days',   '[SEED]'),

-- Tháng này
(gen_random_uuid(), '00000000-0000-0002-0000-000000000028', 999000,  'COMPLETED', 'MOMO',  'SEED-020', NOW()-INTERVAL '8 days', NOW()-INTERVAL '8 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000029', 499000,  'COMPLETED', 'VNPAY', 'SEED-021', NOW()-INTERVAL '5 days', NOW()-INTERVAL '5 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000024', 699000,  'COMPLETED', 'MOMO',  'SEED-022', NOW()-INTERVAL '3 days', NOW()-INTERVAL '3 days', '[SEED]'),
(gen_random_uuid(), '00000000-0000-0002-0000-000000000026', 799000,  'COMPLETED', 'VNPAY', 'SEED-023', NOW()-INTERVAL '1 day',  NOW()-INTERVAL '1 day',  '[SEED]')

ON CONFLICT (payment_transaction_id) DO NOTHING;

-- ============================================================
-- 6. Kết quả kiểm tra
-- ============================================================
SELECT '=== SEED RESULT ===' AS info;
SELECT 'users (SEED)'       AS tbl, COUNT(*) AS cnt FROM users     WHERE notes LIKE '%[SEED]%'
UNION ALL
SELECT 'courses (SEED)',    COUNT(*) FROM courses WHERE course_id::text LIKE 'bb000001%'
UNION ALL
SELECT 'modules (SEED)',    COUNT(*) FROM modules WHERE course_id::text LIKE 'bb000001%'
UNION ALL
SELECT 'enrollments',       COUNT(*) FROM enrollments WHERE user_id::text LIKE '00000000-0000-0002%'
UNION ALL
SELECT 'payments (SEED)',   COUNT(*) FROM payments WHERE payment_notes LIKE '%[SEED]%';

-- Kiểm tra module count per APPROVED course
SELECT c.course_title, c.course_status, COUNT(m.modules_id) AS module_count
FROM courses c
LEFT JOIN modules m ON m.course_id = c.course_id
WHERE c.course_id::text LIKE 'bb000001%'
GROUP BY c.course_id, c.course_title, c.course_status
ORDER BY c.course_status, c.course_created_at;

-- Tổng doanh thu SEED
SELECT 'Total Revenue (SEED): ' || SUM(payment_amount) || ' VND' AS revenue_check
FROM payments WHERE payment_notes LIKE '%[SEED]%' AND payment_status = 'COMPLETED';
