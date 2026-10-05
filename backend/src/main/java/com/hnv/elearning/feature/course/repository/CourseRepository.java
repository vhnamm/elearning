package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    @Override
    Page<Course> findAll(Specification<Course> spec, Pageable pageable);


        Optio

    al<Course> findWithDetai

    
    boolean existsByIdAndInstructorId(Long id, Long instructorId);

    boolean existsByIdAndStatus(Long id, CourseStatus status);

    @EntityGraph(attributePaths = {"learningOutcomes", "requiredSkills"})
    Optional<Course> findWithDetailById(Long id);

    boolean existsByIdAndInstructorId(Long id, Long instructorId);

    @EntityGraph(attributePaths = {"instructor", "category", "subcategory", "subcategory.category"})
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    long countByStatus(CourseStatus status);

    @EntityGraph(attributePaths = {
            "instructor", "category", "subcategory", "subcategory.category", "learningOutcomes", "requiredSkills"
    })
    Optional<Course> findForReviewById(Long id);

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

              )  
            ORDER BY cat.name
            """)
    List<String> findRelatedCategoryNames(@Param("pattern") String

    
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



    @EntityGraph(attributePaths = {"learningOutcomes", "requiredSkills"})
    Optional<Course> findCourseDetailById(Long id);
}
