-- video_key tạm chứa link YouTube (nhúng được), sau này thay bằng R2 key
-- content_item_id 6 và 9 là quiz nên không có lecture
-- jNQXAC9IVRw = Me at the zoo (19s), eRsGyueVLvQ = Sintel, R6MlUcmOul8 = Tears of Steel
INSERT INTO lectures (id, content_item_id, content_type, video_status, video_raw_key, video_key, video_duration_seconds, thumbnail_url) VALUES
(1, 1, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg'),
(2, 2, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'https://img.youtube.com/vi/eRsGyueVLvQ/hqdefault.jpg'),
(3, 3, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'https://img.youtube.com/vi/R6MlUcmOul8/hqdefault.jpg'),
(4, 4, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg'),
(5, 5, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=eRsGyueVLvQ', 888, 'https://img.youtube.com/vi/eRsGyueVLvQ/hqdefault.jpg'),
(6, 7, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=R6MlUcmOul8', 734, 'https://img.youtube.com/vi/R6MlUcmOul8/hqdefault.jpg'),
(7, 8, 'VIDEO', 'READY', NULL, 'https://www.youtube.com/watch?v=jNQXAC9IVRw', 19,  'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg');