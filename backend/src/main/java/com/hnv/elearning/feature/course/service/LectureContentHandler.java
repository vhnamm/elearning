package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.instructor.ContentCompleteRequest;
import com.hnv.elearning.feature.course.dto.instructor.ContentPresignedUploadResponse;
import com.hnv.elearning.feature.course.dto.instructor.ContentUploadRequest;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.enums.LectureType;

public interface LectureContentHandler {
    LectureType type();

    ContentPresignedUploadResponse prepareUpload(CurriculumItem item, ContentUploadRequest request);

    void completeUpload(CurriculumItem item, ContentCompleteRequest request);
}
