package com.hnv.elearning.feature.moderation.service;

import com.hnv.elearning.feature.moderation.dto.ApproveCourseRequest;
import com.hnv.elearning.feature.moderation.dto.CourseReviewDetailResponse;
import com.hnv.elearning.feature.moderation.dto.ModerationResultResponse;
import com.hnv.elearning.feature.moderation.dto.PendingCourseItemDto;
import com.hnv.elearning.feature.moderation.dto.RejectCourseRequest;
import com.hnv.elearning.feature.moderation.dto.RejectReasonOptionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseApproveService {

    Page<PendingCourseItemDto> getPendingCourses(Pageable pageable);

    long countPendingCourses();

    CourseReviewDetailResponse getCourseForReview(Long courseId);

    ModerationResultResponse approveCourse(Long courseId, ApproveCourseRequest request, Long moderatorId);

    ModerationResultResponse rejectCourse(Long courseId, RejectCourseRequest request, Long moderatorId);

    List<RejectReasonOptionDto> getRejectReasons();
}
