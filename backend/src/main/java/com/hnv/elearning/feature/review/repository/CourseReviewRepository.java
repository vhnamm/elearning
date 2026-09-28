package com.hnv.elearning.feature.review.repository;

import com.hnv.elearning.feature.review.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    @Query("SELECT c.id, COALESCE(ROUND(AVG(cr.rating), 1), 0) " +
            "FROM Course c " +
            "LEFT JOIN CourseReview cr ON cr.course = c " +
            "WHERE c.id IN :courseIds " +
            "GROUP BY c.id")
    List<Object[]> getAverageRatingsByCourseIds(@Param("courseIds") Collection<Long> courseIds);

    @Query("SELECT c.id, COUNT(cr.id) " +
            "FROM Course c " +
            "LEFT JOIN CourseReview cr ON cr.course = c " +
            "WHERE c.id IN :courseIds " +
            "GROUP BY c.id")
    List<Object[]> getReviewCountsByCourseIds(@Param("courseIds") Collection<Long> courseIds);
}
