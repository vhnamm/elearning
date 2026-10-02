-- ==============================================================================
-- Bổ sung category_id + timestamp cho các khoá đã PUBLISHED / PENDING_REVIEW
-- (cột category_id được thêm ở V24090454, sau khi V15 seed courses nên V15 chưa có).
-- Khoá DRAFT chưa cần nên giữ nguyên.
--   Course 1 (Spring Boot)  -> subcategory 1  -> category 1 (Lập trình & Phát triển)
--   Course 2 (Figma UI/UX)  -> subcategory 7  -> category 3 (Thiết kế & Đồ họa)
--   Course 4 (HeyGen AI)    -> subcategory 12 -> category 4 (Trí tuệ nhân tạo & Data)
--   Course 5 (IELTS)        -> subcategory 19 -> category 7 (Ngoại ngữ)
-- ==============================================================================

UPDATE courses c
    JOIN subcategories s ON s.id = c.subcategory_id
SET c.category_id = s.category_id
WHERE c.status IN ('PUBLISHED', 'PENDING_REVIEW');

UPDATE courses SET created_at = '2026-06-20 09:00:00', updated_at = '2026-07-01 10:00:00' WHERE id = 1;
UPDATE courses SET created_at = '2026-06-25 09:00:00', updated_at = '2026-07-05 10:00:00' WHERE id = 2;
UPDATE courses SET created_at = '2026-09-10 09:00:00', updated_at = '2026-09-15 10:00:00' WHERE id = 4;
UPDATE courses SET created_at = '2026-07-10 09:00:00', updated_at = '2026-07-20 10:00:00' WHERE id = 5;
