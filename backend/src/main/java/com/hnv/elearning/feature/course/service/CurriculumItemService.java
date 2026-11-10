package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.privates.*;

public interface CurriculumItemService {
    CurriculumItemCreateResponse createItem(Long sectionId, Long instructorId, CurriculumItemCreateRequest request);
    void deleteItem(Long sectionId, Long instructorId, Long itemId);
    void updateItem(Long itemId, Long instructorId, CurriculumItemUpdateRequest request);
    ContentPresignedUploadResponse getPresignedContentUpload(Long itemId, Long instructorId, ContentUploadRequest request);
    void completeContentUpload(ContentCompleteRequest request, Long itemId, Long instructorId);
    ContentAccessResponse getContentAccess(Long itemId, Long instructorId);

}
