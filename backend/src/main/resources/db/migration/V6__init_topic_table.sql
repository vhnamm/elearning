-- 3. Table: topics (Chủ đề cấp 3, gắn thẻ vào khoá học)
CREATE TABLE topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subcategory_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    proposed_by_user_id BIGINT NULL,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_topics_subcategory_id
        FOREIGN KEY (subcategory_id) REFERENCES subcategories (id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_topics_proposed_by_user_id
        FOREIGN KEY (proposed_by_user_id) REFERENCES users (id)
            ON DELETE SET NULL ON UPDATE CASCADE,

-- Đảm bảo slug duy nhất trong cùng một subcategory_id
    CONSTRAINT uq_topics_subcategory_slug UNIQUE (subcategory_id, slug)
) ENGINE = InnoDB;