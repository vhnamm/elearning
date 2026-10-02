-- ==============================================================================
-- 4. LECTURES (Curriculum items có type = 'LECTURE')
-- YouTube sample IDs:
--   jNQXAC9IVRw (19s), eRsGyueVLvQ (888s), R6MlUcmOul8 (734s)
-- ==============================================================================
INSERT INTO lectures (
    id, curriculum_item_id, content_type, video_status,
    video_raw_key, video_key, video_duration_seconds, thumbnail_url
) VALUES
-- Lectures của Khóa 1 (Spring Boot)
(1, 1,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg'),
(2, 2,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'https://img.youtube.com/vi/eRsGyueVLvQ/hqdefault.jpg'),
(3, 3,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'https://img.youtube.com/vi/R6MlUcmOul8/hqdefault.jpg'),
(4, 4,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg'),
(5, 5,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'https://img.youtube.com/vi/eRsGyueVLvQ/hqdefault.jpg'),
(6, 7,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'https://img.youtube.com/vi/R6MlUcmOul8/hqdefault.jpg'),
(7, 8,  'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg'),

-- Lectures của Khóa 4 (HeyGen AI - Chờ duyệt, bài giảng đã tải lên và xử lý xong)
(8, 10, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg'),
(9, 11, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'https://img.youtube.com/vi/eRsGyueVLvQ/hqdefault.jpg'),
(10, 13, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'https://img.youtube.com/vi/R6MlUcmOul8/hqdefault.jpg'),
(11, 14, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg');