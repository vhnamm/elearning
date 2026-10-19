-- ==============================================================================
-- 4. LECTURES (Curriculum items có type = 'LECTURE')
-- YouTube sample IDs:
--   jNQXAC9IVRw (19s), eRsGyueVLvQ (888s), R6MlUcmOul8 (734s)
-- ==============================================================================
INSERT INTO lectures (
    id, curriculum_item_id, content_type, video_status,
    video_raw_key, video_key, video_duration_seconds, thumbnail_key
) VALUES
-- thumbnail_key: object key trong bucket PUBLIC, dạng courses/{courseId}/lectures/{curriculumItemId}/thumbnail/seed.jpg
-- (OLD: URL ảnh YouTube https://img.youtube.com/vi/<id>/hqdefault.jpg; ảnh cần được upload thủ công lên R2 theo đúng key)
-- Lectures của Khóa 1 (Spring Boot)
(1, 1,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'courses/1/lectures/1/thumbnail/seed.jpg'),
(2, 2,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'courses/1/lectures/2/thumbnail/seed.jpg'),
(3, 3,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'courses/1/lectures/3/thumbnail/seed.jpg'),
(4, 4,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'courses/1/lectures/4/thumbnail/seed.jpg'),
(5, 5,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'courses/1/lectures/5/thumbnail/seed.jpg'),
(6, 7,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'courses/1/lectures/7/thumbnail/seed.jpg'),
(7, 8,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'courses/1/lectures/8/thumbnail/seed.jpg'),

-- Lectures của Khóa 4 (HeyGen AI - Chờ duyệt, bài giảng đã tải lên và xử lý xong)
(8, 10, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'courses/4/lectures/10/thumbnail/seed.jpg'),
(9, 11, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'courses/4/lectures/11/thumbnail/seed.jpg'),
(10, 13, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'courses/4/lectures/13/thumbnail/seed.jpg'),
(11, 14, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'courses/4/lectures/14/thumbnail/seed.jpg');