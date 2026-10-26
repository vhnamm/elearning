package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.instructor.ContentCompleteRequest;
import com.hnv.elearning.feature.course.dto.instructor.ContentPresignedUploadResponse;
import com.hnv.elearning.feature.course.dto.instructor.ContentUploadRequest;
import com.hnv.elearning.feature.course.dto.instructor.CurriculumItemCreateRequest;
import com.hnv.elearning.feature.course.dto.instructor.CurriculumItemCreateResponse;

public interface CurriculumItemService {
    CurriculumItemCreateResponse createItem(Long sectionId, Long instructorId, CurriculumItemCreateRequest request);
    void deleteItem(Long sectionId, Long instructorId, Long itemId);
    ContentPresignedUploadResponse getPresignedContentUpload(Long itemId, Long instructorId, ContentUploadRequest request);
    void completeContentUpload(ContentCompleteRequest request, Long itemId, Long instructorId);
}
