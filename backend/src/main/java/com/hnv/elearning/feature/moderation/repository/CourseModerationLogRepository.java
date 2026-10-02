package com.hnv.elearning.feature.moderation.repository;

import com.hnv.elearning.feature.moderation.entity.CourseModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseModerationLogRepository extends JpaRepository<CourseModerationLog, Long> {
}
