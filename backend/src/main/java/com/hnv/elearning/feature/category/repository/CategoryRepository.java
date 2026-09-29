package com.hnv.elearning.feature.category.repository;

import com.hnv.elearning.feature.category.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    List<Category> findAll();

    // Tên danh mục cha có chứa từ khóa, dùng khi không có khóa học khớp.
    @Query("""
            SELECT c.name
            FROM Category c
            WHERE LOWER(c.name) LIKE :pattern ESCAPE '\\'
            ORDER BY c.name
            """)
    List<String> findNamesByKeyword(@Param("pattern") String pattern, Pageable pageable);
}
