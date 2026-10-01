package com.hnv.elearning.feature.enrollment.controller;

import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/check/{courseId}")
    public ResponseEntity<Boolean> checkEnrollment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User user
    ) {
        boolean hasBought = enrollmentService.checkUserEnrolled(courseId, user.getId());
        return ResponseEntity.ok(hasBought);
    }

    @PostMapping("/free/{courseId}")
    public ResponseEntity<String> enrollFreeCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User user
    ) {
        enrollmentService.enrollFreeCourse(courseId, user.getId());
        return ResponseEntity.ok("Đăng ký khóa học miễn phí thành công!");
    }
}