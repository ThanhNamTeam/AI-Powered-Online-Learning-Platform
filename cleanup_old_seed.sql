-- ============================================================
-- cleanup_old_seed.sql
-- Xoa cac courses cu dung gen_random_uuid() (khong co modules)
-- Giu lai courses co fixed UUID: bb000001-... va aa000001-...
-- ============================================================

-- 1. Xoa payments linked toi enrollments cua courses cu
DELETE FROM payments
WHERE enrollment_id IN (
  SELECT e.enrollments_id
  FROM enrollments e
  JOIN courses c ON e.course_id = c.course_id
  WHERE c.course_constructor_id IN (
    '00000000-0000-0001-0000-000000000010',
    '00000000-0000-0001-0000-000000000011',
    '00000000-0000-0001-0000-000000000012',
    '00000000-0000-0001-0000-000000000013'
  )
  AND c.course_id::text NOT LIKE 'bb000001%'
);

-- 2. Xoa enrollments cua courses cu
DELETE FROM enrollments
WHERE course_id IN (
  SELECT course_id FROM courses
  WHERE course_constructor_id IN (
    '00000000-0000-0001-0000-000000000010',
    '00000000-0000-0001-0000-000000000011',
    '00000000-0000-0001-0000-000000000012',
    '00000000-0000-0001-0000-000000000013'
  )
  AND course_id::text NOT LIKE 'bb000001%'
);

-- 3. Xoa modules cua courses cu (neu co)
DELETE FROM modules
WHERE course_id IN (
  SELECT course_id FROM courses
  WHERE course_constructor_id IN (
    '00000000-0000-0001-0000-000000000010',
    '00000000-0000-0001-0000-000000000011',
    '00000000-0000-0001-0000-000000000012',
    '00000000-0000-0001-0000-000000000013'
  )
  AND course_id::text NOT LIKE 'bb000001%'
);

-- 4. Xoa courses cu (random UUID)
DELETE FROM courses
WHERE course_constructor_id IN (
  '00000000-0000-0001-0000-000000000010',
  '00000000-0000-0001-0000-000000000011',
  '00000000-0000-0001-0000-000000000012',
  '00000000-0000-0001-0000-000000000013'
)
AND course_id::text NOT LIKE 'bb000001%';

-- Ket qua sau cleanup
SELECT 'Total courses' AS info, COUNT(*) AS cnt FROM courses
UNION ALL
SELECT 'Total modules', COUNT(*) FROM modules
UNION ALL
SELECT 'APPROVED courses', COUNT(*) FROM courses WHERE course_status = 'APPROVED'
UNION ALL
SELECT 'PENDING courses',  COUNT(*) FROM courses WHERE course_status = 'PENDING_APPROVAL';

-- Kiem tra module count per course
SELECT c.course_title, c.course_status, COUNT(m.modules_id) AS module_count
FROM courses c
LEFT JOIN modules m ON m.course_id = c.course_id
GROUP BY c.course_id, c.course_title, c.course_status
ORDER BY c.course_status, module_count DESC;
