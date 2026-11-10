package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureRepository extends JpaRepository<Lecture,Long> {
    @Query("""
        SELECT COUNT(l.id) > 0 FROM Lecture l 
        WHERE l.videoKey = :key OR l.videoRawKey = :key
            OR l.fileKey = :key OR l.thumbnailKey = :key
    """)
    boolean existsByAnyKey(@Param("key") String key);
}
