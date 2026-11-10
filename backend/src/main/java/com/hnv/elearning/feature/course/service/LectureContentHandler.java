package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.privates.ContentAccessResponse;
import com.hnv.elearning.feature.course.dto.privates.ContentCompleteRequest;
import com.hnv.elearning.feature.course.dto.privates.ContentPresignedUploadResponse;
import com.hnv.elearning.feature.course.dto.privates.ContentUploadRequest;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.enums.LectureType;

public interface LectureContentHandler {
    LectureType type();

    ContentPresignedUploadResponse prepareUpload(CurriculumItem item, ContentUploadRequest request);
    ContentAccessResponse createAccess(CurriculumItem item);
    void completeUpload(CurriculumItem item, ContentCompleteRequest request);
}
