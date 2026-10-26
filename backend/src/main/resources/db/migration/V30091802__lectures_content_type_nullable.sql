-- Lecture được tạo cùng curriculum item ("vỏ"). content_type (VIDEO/DOCUMENT) và video_status
-- chỉ có giá trị khi giảng viên bấm "+ Nội dung" và chọn loại nội dung.
ALTER TABLE lectures
    MODIFY content_type ENUM('VIDEO', 'DOCUMENT') NULL,
    MODIFY video_status ENUM('UPLOADING', 'PROCESSING', 'READY', 'FAILED') NULL DEFAULT NULL;
