-- 9. Bảng chi tiết các khóa học trong đơn hàng
CREATE TABLE order_items (
     id                BIGINT AUTO_INCREMENT,
     order_id          BIGINT         NOT NULL,
     course_id         BIGINT         NOT NULL,
     price_at_purchase DECIMAL(10, 2) NOT NULL,

     PRIMARY KEY (id),
     CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
     CONSTRAINT fk_order_items_course FOREIGN KEY (course_id) REFERENCES courses (id),
     CONSTRAINT uq_order_course UNIQUE (order_id, course_id)
) ENGINE=InnoDB;