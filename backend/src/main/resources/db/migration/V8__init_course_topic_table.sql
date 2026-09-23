
CREATE TABLE course_topics (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   course_id BIGINT NOT NULL,
   topic_id BIGINT NOT NULL,

-- Ràng buộc UNIQUE để chống trùng lặp (1 khóa học không thể gắn 1 topic 2 lần)
   CONSTRAINT uq_course_topics_course_topic UNIQUE (course_id, topic_id),

-- Khóa ngoại tới bảng courses
   CONSTRAINT fk_course_topics_course_id
       FOREIGN KEY (course_id) REFERENCES courses (id)
           ON DELETE CASCADE ON UPDATE CASCADE,

-- Khóa ngoại tới bảng topics
   CONSTRAINT fk_course_topics_topic_id
       FOREIGN KEY (topic_id) REFERENCES topics (id)
           ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;
