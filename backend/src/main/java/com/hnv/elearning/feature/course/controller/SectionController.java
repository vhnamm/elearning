package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.instructor.SectionCreateRequest;
import com.hnv.elearning.feature.course.dto.instructor.SectionCreateResponse;
import com.hnv.elearning.feature.course.dto.instructor.SectionReorderRequest;
import com.hnv.elearning.feature.course.service.SectionService;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SectionController {
    private final SectionService sectionService;

    @PostMapping("/api/v1/courses/{courseId}/versions/{versionId}/sections")
    public ResponseEntity<ApiResponse<SectionCreateResponse>> createSection(
            @PathVariable("courseId") Long courseId,
            @PathVariable("versionId")  Long versionId,
            @AuthenticationPrincipal User user,
            @RequestBody SectionCreateRequest sectionCreateRequest
    ) {
        SectionCreateResponse rs = sectionService.createSection(sectionCreateRequest, courseId, versionId, user.getId());
        ApiResponse response = ApiResponse.builder()
                .data(rs)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/v1/courses/{courseId}/versions/{versionId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable("courseId") Long courseId,
            @PathVariable("versionId") Long versionId,
            @PathVariable("sectionId") Long sectionId,
            @AuthenticationPrincipal User user
    ){
        sectionService.deleteSection(courseId, versionId, sectionId, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.NO_CONTENT.value())
                        .message("Xóa section thành công")
                        .build()
        );

    }

    @PatchMapping("/api/v1/courses/{courseId}/versions/{versionId}/sections/position")
    public ResponseEntity<ApiResponse<Void>> reorderSection(
            @PathVariable("courseId") Long courseId,
            @PathVariable("versionId") Long versionId,
            @AuthenticationPrincipal User user,
            @RequestBody SectionReorderRequest request
    ){
        sectionService.reorderSection(courseId, versionId, user.getId(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.NO_CONTENT.value())
                        .message("Thay đổi thứ tự section thành công")
                        .build()
        );
    }
}
