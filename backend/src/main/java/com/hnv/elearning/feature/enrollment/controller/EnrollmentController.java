package com.hnv.elearning.feature.enrollment.controller;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.enrollment.dto.EnrollmentRequest;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/{courseId}/status")
    public ResponseEntity<ApiResponse<Boolean>> getEnrollmentStatus(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User user
    ) {
        boolean enrolled = enrollmentService.checkUserEnrolled(courseId, user.getId());
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .code(HttpStatus.OK.value())
                        .data(enrolled)
                        .build()
        );
    }

    //CHỈ DÀNH CHO KHOÁ FREE, MUA THẬT THÌ PHẢI XỬ LÝ Ở CALLBACK
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> enroll(
            @RequestBody EnrollmentRequest request,
            @AuthenticationPrincipal User user
    ) {
        if (request.getCourseId() == null) {
            throw new AppException(ErrorCode.COURSE_ID_REQUIRED);
        }
        enrollmentService.enrollFreeCourse(request.getCourseId(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Đăng ký khóa học miễn phí thành công!")
                        .build()
        );
    }
}
