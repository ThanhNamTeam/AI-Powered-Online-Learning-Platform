-- ============================================================
-- SEED: Cập nhật thumbnail_url cho tất cả courses
-- và thêm dữ liệu mới nếu thiếu
-- Chạy: psql -U postgres -d swd392_db -f seed_courses_thumbnail.sql
-- ============================================================

-- 1. Cập nhật thumbnail cho 12 courses đã có (bb000001...)
UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000001'; -- Hiragana & Katakana

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000002'; -- Minna no Nihongo N5

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1604872441539-ef1db9b25f92?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000003'; -- JLPT N4

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1556761175-4b46a572b786?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000004'; -- N3 Business

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000005'; -- JLPT N2

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1564981797816-1043664bf78d?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000006'; -- JLPT N1

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1600880292203-757bb62b4baf?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000007'; -- Business Japanese

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000008'; -- Kaiwa

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000009'; -- Kanji N3

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000010'; -- Luyện nghe N2

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1642132652860-471b4228023d?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000011'; -- Anime & Manga

UPDATE courses SET course_thumbnail_url = 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&q=80'
WHERE course_id = 'bb000001-0000-0000-0000-000000000012'; -- Du học Nhật

-- ============================================================
-- 2. Nếu seed_data.sql chưa được chạy, chạy toàn bộ từ đầu
--    (uncomment block dưới nếu DB còn trống)
-- ============================================================

-- Kiểm tra số courses hiện tại
DO $$
DECLARE
    course_count INTEGER;
    user_count   INTEGER;
BEGIN
    SELECT COUNT(*) INTO course_count FROM courses WHERE course_id::text LIKE 'bb000001%';
    SELECT COUNT(*) INTO user_count   FROM users   WHERE notes LIKE '%[SEED]%';

    RAISE NOTICE '✅ Courses (bb000001): % rows', course_count;
    RAISE NOTICE '✅ Users (SEED):        % rows', user_count;

    IF course_count = 0 THEN
        RAISE NOTICE '⚠️  Không có course nào! Hãy chạy seed_data.sql trước.';
    END IF;
END $$;

-- ============================================================
-- 3. Đăng ký user THẬT (tài khoản bạn đang dùng) vào courses
--    Thay YOUR_EMAIL bằng email tài khoản student của bạn
-- ============================================================

-- Lấy user_id từ email của bạn và đăng ký vào tất cả APPROVED courses
INSERT INTO enrollments (enrollments_id, user_id, course_id, enrollments_enrolled_at, status)
SELECT
    gen_random_uuid(),
    u.user_id,
    c.course_id,
    NOW() - (RANDOM() * INTERVAL '3 months'),
    'ACTIVE'
FROM users u
CROSS JOIN courses c
WHERE u.email NOT LIKE '%seed%'          -- Chỉ user thật (không phải seed)
  AND u.role = 'STUDENT'
  AND c.course_status = 'APPROVED'
  AND c.course_id::text LIKE 'bb000001%'
  AND NOT EXISTS (
      SELECT 1 FROM enrollments e
      WHERE e.user_id = u.user_id AND e.course_id = c.course_id
  )
ON CONFLICT DO NOTHING;

-- ============================================================
-- 4. Kiểm tra kết quả
-- ============================================================
SELECT
    c.course_title,
    c.course_status,
    c.course_jlpt_level AS level,
    c.course_price      AS price,
    CASE WHEN c.course_thumbnail_url IS NOT NULL THEN '✅ có' ELSE '❌ thiếu' END AS thumbnail,
    COUNT(e.enrollments_id) AS students
FROM courses c
LEFT JOIN enrollments e ON e.course_id = c.course_id
WHERE c.course_id::text LIKE 'bb000001%'
GROUP BY c.course_id, c.course_title, c.course_status, c.course_jlpt_level, c.course_price, c.course_thumbnail_url
ORDER BY c.course_status, c.course_jlpt_level;
