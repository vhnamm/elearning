package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.CurriculumItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CurriculumItemRepository extends JpaRepository<CurriculumItem, Long> {

    @Query("SELECT COALESCE(MAX(i.position), 0) FROM CurriculumItem i WHERE i.section.id = :sectionId")
    int findMaxPosition(@Param("sectionId") Long sectionId);

    @Query("""
        SELECT ci FROM CurriculumItem ci
        JOIN FETCH ci.section s 
        JOIN FETCH s.courseVersion v 
        WHERE ci.id = :itemId
            AND s.id = :sectionId
            AND v.course.instructor.id = :instructorId
    """)
    Optional<CurriculumItem> findOwnedItem(@Param("sectionId") Long sectionId, @Param("itemId") Long itemId, @Param("instructorId") Long instructorId);

    @Query("""
        SELECT ci FROM CurriculumItem ci
        JOIN FETCH ci.section s
        JOIN FETCH s.courseVersion v
        JOIN FETCH v.course c
        WHERE ci.id = :itemId
            AND v.course.instructor.id = :instructorId
    """)
    Optional<CurriculumItem> findOwnedItemById(@Param("itemId") Long itemId, @Param("instructorId") Long instructorId);

    @Query("UPDATE CurriculumItem ci SET ci.position = ci.position - 1 WHERE ci.section.id = :sectionId AND ci.position > :position")
    @Modifying
    void shiftPositionAfterDelete(@Param("sectionId") Long sectionId, @Param("position") int position);

}
