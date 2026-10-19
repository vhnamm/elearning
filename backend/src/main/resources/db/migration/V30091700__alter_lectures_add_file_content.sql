-- Bổ sung cột cho bài đọc (content_type = 'DOCUMENT') và thông tin file video.
-- video_status chỉ có nghĩa với VIDEO nên cho phép NULL (bài đọc không có vòng đời xử lý).
ALTER TABLE lectures
    MODIFY COLUMN video_status ENUM('UPLOADING', 'PROCESSING', 'READY', 'FAILED') NULL DEFAULT NULL,
    ADD COLUMN video_size_bytes     BIGINT        NULL AFTER video_duration_seconds,
    ADD COLUMN file_key             VARCHAR(1024) NULL AFTER thumbnail_key,
    ADD COLUMN file_name            VARCHAR(255)  NULL AFTER file_key,
    ADD COLUMN file_size_bytes      BIGINT        NULL AFTER file_name,
    ADD COLUMN file_mime_type       VARCHAR(127)  NULL AFTER file_size_bytes,
    ADD COLUMN updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
