package com.hnv.elearning.feature.category.repository;

import com.hnv.elearning.feature.category.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Page<Topic> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
