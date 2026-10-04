package com.hnv.elearning.feature.category.repository;

import com.hnv.elearning.feature.category.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Page<Topic> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query(""" 
        SELECT t FROM Course c 
        JOIN c.topics t 
        JOIN Enrollment e ON e.course = c 
        WHERE c.status = com.hnv.elearning.feature.course.enums.CourseStatus.PUBLISHED
            AND t.status = com.hnv.elearning.feature.category.enums.TopicStatus.APPROVED
            AND e.status <> com.hnv.elearning.feature.enrollment.enums.EnrollmentStatus.REVOKED
            AND e.enrolledAt >=  CURRENT_DATE - 90 day
        GROUP BY t.id, t.name
        ORDER BY COUNT(e) DESC, t.name ASC
        LIMIT :limit
    """)
    List<Topic> getRecentTrendingTopics(@Param("limit") Long limit);
}
