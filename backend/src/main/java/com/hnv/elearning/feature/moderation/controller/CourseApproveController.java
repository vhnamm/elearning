package com.hnv.elearning.feature.moderation.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.moderation.dto.ApproveCourseRequest;
import com.hnv.elearning.feature.moderation.dto.CourseReviewDetailResponse;
import com.hnv.elearning.feature.moderation.dto.ModerationResultResponse;
import com.hnv.elearning.feature.moderation.dto.PendingCourseItemDto;
import com.hnv.elearning.feature.moderation.dto.RejectCourseRequest;
import com.hnv.elearning.feature.moderation.dto.RejectReasonOptionDto;
import com.hnv.elearning.feature.moderation.service.CourseApproveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/courses")
public class CourseApproveController {
    private final CourseApproveService courseApproveService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<PendingCourseItemDto>>> getPendingCourses(
            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ok(courseApproveService.getPendingCourses(pageable));
    }

    @GetMapping("/pending/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countPendingCourses() {
        return ok(Map.of("count", courseApproveService.countPendingCourses()));
    }

    @GetMapping("/reject-reasons")
    public ResponseEntity<ApiResponse<List<RejectReasonOptionDto>>> getRejectReasons() {
        return ok(courseApproveService.getRejectReasons());
    }

    @GetMapping("/{id}/review")
    public ResponseEntity<ApiResponse<CourseReviewDetailResponse>> getCourseForReview(
            @PathVariable(name = "id") Long courseId
    ) {
        return ok(courseApproveService.getCourseForReview(courseId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ModerationResultResponse>> approveCourse(
            @PathVariable(name = "id") Long courseId,
            @RequestBody(required = false) ApproveCourseRequest request,
            @AuthenticationPrincipal(expression = "id") Long moderatorId
    ) {
        return ok(courseApproveService.approveCourse(courseId, request, moderatorId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ModerationResultResponse>> rejectCourse(
            @PathVariable(name = "id") Long courseId,
            @RequestBody RejectCourseRequest request,
            @AuthenticationPrincipal(expression = "id") Long moderatorId
    ) {
        return ok(courseApproveService.rejectCourse(courseId, request, moderatorId));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
