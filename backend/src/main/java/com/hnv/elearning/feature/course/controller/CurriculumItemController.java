package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.instructor.ContentCompleteRequest;
import com.hnv.elearning.feature.course.dto.instructor.ContentPresignedUploadResponse;
import com.hnv.elearning.feature.course.dto.instructor.ContentUploadRequest;
import com.hnv.elearning.feature.course.dto.instructor.CurriculumItemCreateRequest;
import com.hnv.elearning.feature.course.dto.instructor.CurriculumItemCreateResponse;
import com.hnv.elearning.feature.course.service.CurriculumItemService;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CurriculumItemController {
    private final CurriculumItemService curriculumItemService;

    @PostMapping("/api/v1/sections/{sectionId}/items")
    public ResponseEntity<ApiResponse<CurriculumItemCreateResponse>> createItem(
            @PathVariable("sectionId") Long sectionId,
            @AuthenticationPrincipal User user,
            @RequestBody CurriculumItemCreateRequest request
    ) {
        CurriculumItemCreateResponse rs = curriculumItemService.createItem(sectionId, user.getId(), request);
        ApiResponse<CurriculumItemCreateResponse> response = ApiResponse.<CurriculumItemCreateResponse>builder()
                .data(rs)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/v1/sections/{sectionId}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable("sectionId") Long sectionId,
            @PathVariable("itemId") Long itemId,
            @AuthenticationPrincipal User user
    ){
        curriculumItemService.deleteItem(sectionId, user.getId(), itemId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Xoa thanh cong")
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PostMapping("/api/v1/curriculum-items/{itemId}/content/presigned-url")
    public ResponseEntity<ApiResponse<ContentPresignedUploadResponse>> generatePresignedUrl(
            @AuthenticationPrincipal User user,
            @PathVariable("itemId") Long itemId,
            @RequestBody ContentUploadRequest request
    ){
        ContentPresignedUploadResponse rs = curriculumItemService.getPresignedContentUpload(itemId, user.getId(), request);
        ApiResponse<ContentPresignedUploadResponse> response = ApiResponse.<ContentPresignedUploadResponse>builder()
                .data(rs)
                .code(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/api/v1/curriculum-items/{itemId}/content/complete")
    public ResponseEntity<ApiResponse<Void>> completeUploadContent(
            @PathVariable("itemId") Long itemId,
            @AuthenticationPrincipal User user,
            @RequestBody ContentCompleteRequest request
    ){
        curriculumItemService.completeContentUpload(request, itemId, user.getId());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
