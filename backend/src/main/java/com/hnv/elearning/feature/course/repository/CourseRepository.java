package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    @Override
    Page<Course> findAll(Specification<Course> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"learningOutcomes", "requiredSkills"})
    Optional<Course> findWithDetailById(Long id);

    boolean existsByIdAndInstructorId(Long id, Long instructorId);

    // Danh mục cha có nhiều khóa đã xuất bản nhất, dùng khi chưa nhập từ khóa.
    @Query("""
            SELECT cat.name
            FROM Course c
            JOIN c.category cat
            WHERE c.status = com.hnv.elearning.feature.course.enums.CourseStatus.PUBLISHED
            GROUP BY cat.id, cat.name
            ORDER BY COUNT(c.id) DESC, cat.name ASC
            """)
    List<String> findPopularCategoryNames(Pageable pageable);

    // Tên danh mục cha của các khóa đã xuất bản khớp từ khóa.
    @Query("""
            SELECT DISTINCT cat.name
            FROM Course c
            JOIN c.category cat
            LEFT JOIN c.subcategory s
            WHERE c.status = com.hnv.elearning.feature.course.enums.CourseStatus.PUBLISHED
              AND (
                LOWER(c.title) LIKE :pattern ESCAPE '\\'
                OR LOWER(COALESCE(c.shortDescription, '')) LIKE :pattern ESCAPE '\\'
                OR LOWER(COALESCE(s.name, '')) LIKE :pattern ESCAPE '\\'
                OR LOWER(cat.name) LIKE :pattern ESCAPE '\\'
              )
            ORDER BY cat.name
            """)
    List<String> findRelatedCategoryNames(@Param("pattern") String pattern, Pageable pageable);

    // Tên danh mục con của các khóa đã xuất bản khớp từ khóa.
    @Query("""
            SELECT DISTINCT s.name
            FROM Course c
            JOIN c.subcategory s
            WHERE c.status = com.hnv.elearning.feature.course.enums.CourseStatus.PUBLISHED
              AND (
                LOWER(c.title) LIKE :pattern ESCAPE '\\'
                OR LOWER(COALESCE(c.shortDescription, '')) LIKE :pattern ESCAPE '\\'
                OR LOWER(s.name) LIKE :pattern ESCAPE '\\'
              )
            ORDER BY s.name
            """)
    List<String> findRelatedSubcategoryNames(@Param("pattern") String pattern, Pageable pageable);

    @EntityGraph(attributePaths = {"sections", "learningOutcomes", "instructor", "requiredSkills"})
    Optional<Course> findCourseDetailById(Long id);
}
