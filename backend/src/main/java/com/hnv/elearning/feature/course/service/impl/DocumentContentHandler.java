package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.dto.instructor.ContentCompleteRequest;
import com.hnv.elearning.feature.course.dto.instructor.ContentPresignedUploadResponse;
import com.hnv.elearning.feature.course.dto.instructor.ContentUploadRequest;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.entity.Lecture;
import com.hnv.elearning.feature.course.enums.LectureType;
import com.hnv.elearning.feature.course.service.LectureContentHandler;
import com.hnv.elearning.infrastructure.storage.BucketType;
import com.hnv.elearning.infrastructure.storage.PresignedUploadResponse;
import com.hnv.elearning.infrastructure.storage.StorageKeyFactory;
import com.hnv.elearning.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentContentHandler implements LectureContentHandler {
    private static final long MAX_SIZE_BYTES = 100L * 1024 * 1024;

    @Value("${r2.ttl.document-upload}")
    private Duration UPLOAD_TTL;

    private final StorageService storageService;
    private final StorageKeyFactory storageKeyFactory;

    @Override
    public LectureType type() {
        return LectureType.DOCUMENT;
    }

    @Override
    public ContentPresignedUploadResponse prepareUpload(CurriculumItem item, ContentUploadRequest request) {
        Long fileSize = request.getFileSizeBytes();
        if (fileSize == null || fileSize <= 0 || fileSize > MAX_SIZE_BYTES) {
            throw new AppException(ErrorCode.FILE_SIZE_INVALID);
        }

        Long courseId = item.getSection().getCourseVersion().getCourse().getId();
        String key = storageKeyFactory.lectureDocumentKeyGenerate(courseId, item.getStableKey(), UUID.randomUUID().toString(), request.getContentType());

        Lecture lecture = item.getLecture();
        lecture.setContentType(LectureType.DOCUMENT);
        lecture.setFileKey(key);
        lecture.setFileName(request.getFileName());
        lecture.setFileSizeBytes(fileSize);
        lecture.setFileMimeType(request.getContentType());
        lecture.setVideoRawKey(null);
        lecture.setVideoKey(null);
        lecture.setVideoStatus(null);
        lecture.setVideoDurationSeconds(null);
        lecture.setVideoSizeBytes(null);
        lecture.setThumbnailKey(null);

        PresignedUploadResponse doc = storageService.presignedUpload(BucketType.PRIVATE, key, request.getContentType(), UPLOAD_TTL);
        return ContentPresignedUploadResponse.builder()
                .uploadUrl(doc.getUploadUrl())
                .objectKey(doc.getObjectKey())
                .build();
    }

    @Override
    public void completeUpload(CurriculumItem item, ContentCompleteRequest request) {
        Lecture lecture = item.getLecture();
        if (lecture.getContentType() != LectureType.DOCUMENT || lecture.getFileKey() == null) {
            throw new AppException(ErrorCode.UPLOAD_NOT_STARTED);
        }
        if (!storageService.exist(BucketType.PRIVATE, lecture.getFileKey())) {
            throw new AppException(ErrorCode.CONTENT_NOT_UPLOADED);
        }
    }
}
