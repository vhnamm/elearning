-- 2. Table: subcategories (Danh mục con cấp 2)
CREATE TABLE subcategories (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   category_id BIGINT NOT NULL,
   name VARCHAR(255) NOT NULL,
   slug VARCHAR(255) NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_subcategories_category_id
       FOREIGN KEY (category_id) REFERENCES categories (id)
           ON DELETE RESTRICT ON UPDATE CASCADE,

-- Đảm bảo slug chỉ duy nhất trong phạm vi cùng một category_id
   CONSTRAINT uq_subcategories_category_slug UNIQUE (category_id, slug)
) ENGINE = InnoDB;