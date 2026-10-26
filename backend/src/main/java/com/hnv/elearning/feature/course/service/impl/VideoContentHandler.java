package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.dto.instructor.ContentCompleteRequest;
import com.hnv.elearning.feature.course.dto.instructor.ContentPresignedUploadResponse;
import com.hnv.elearning.feature.course.dto.instructor.ContentUploadRequest;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.entity.Lecture;
import com.hnv.elearning.feature.course.enums.LectureType;
import com.hnv.elearning.feature.course.enums.LectureVideoStatus;
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
public class VideoContentHandler implements LectureContentHandler {
    private long MAX_SIZE_BYTES = 1L * 1024 * 1024 * 1024;
    @Value("${r2.ttl.video-upload}")
    private Duration UPLOAD_TTL;
    @Value("${r2.ttl.image-upload}")
    private Duration THUMBNAIL_UPLOAD_TTL;

    private final StorageService storageService;
    private final StorageKeyFactory storageKeyFactory;

    @Override
    public LectureType type() {
       return  LectureType.VIDEO;
    }

    @Override
    public ContentPresignedUploadResponse prepareUpload(CurriculumItem item, ContentUploadRequest request) {
        //check dung luong file co qua 1gb ko
        Long fileSize = request.getFileSizeBytes();
        if (fileSize == null || fileSize > MAX_SIZE_BYTES || fileSize <= 0) {
            throw new AppException(ErrorCode.FILE_SIZE_INVALID);
        }

        Long courseId = item.getSection().getCourseVersion().getCourse().getId();
        String uploadId = UUID.randomUUID().toString();
        String key = storageKeyFactory.lectureVideoRawKeyGenerate(courseId, item.getStableKey(), uploadId, request.getContentType());
        String thumbKey = request.getThumbnailContentType() == null ? null
                : storageKeyFactory.lectureThumbnailKeyGenerate(courseId, item.getStableKey(), uploadId, request.getThumbnailContentType());

        Lecture lecture = item.getLecture();
        lecture.setContentType(LectureType.VIDEO);
        lecture.setVideoRawKey(key);
        lecture.setVideoKey(null);
        lecture.setVideoStatus(LectureVideoStatus.UPLOADING);
        lecture.setVideoDurationSeconds(null);
        lecture.setThumbnailKey(thumbKey);
        lecture.setVideoSizeBytes(fileSize);
        lecture.setFileName(request.getFileName());
        lecture.setFileKey(null);
        lecture.setFileSizeBytes(null);
        lecture.setFileMimeType(null);

        PresignedUploadResponse video = storageService.presignedUpload(BucketType.PRIVATE, key, request.getContentType(), UPLOAD_TTL);
        ContentPresignedUploadResponse.ContentPresignedUploadResponseBuilder response = ContentPresignedUploadResponse.builder()
                .uploadUrl(video.getUploadUrl())
                .objectKey(video.getObjectKey());

        if (thumbKey != null) {
            PresignedUploadResponse thumb = storageService.presignedUpload(BucketType.PUBLIC, thumbKey, request.getThumbnailContentType(), THUMBNAIL_UPLOAD_TTL);
            response.thumbnailUploadUrl(thumb.getUploadUrl()).thumbnailKey(thumb.getObjectKey());
        }
        return response.build();
    }

    @Override
    public void completeUpload(CurriculumItem item, ContentCompleteRequest request) {
        Lecture lecture = item.getLecture();
        if (lecture.getContentType() != LectureType.VIDEO || lecture.getVideoRawKey() == null) {
            throw new AppException(ErrorCode.UPLOAD_NOT_STARTED);
        }
        Integer duration = request.getDurationSeconds();
        if (duration == null || duration < 0) {
            throw new AppException(ErrorCode.VIDEO_DURATION_INVALID);
        }
        if (!storageService.exist(BucketType.PRIVATE, lecture.getVideoRawKey())) {
            throw new AppException(ErrorCode.CONTENT_NOT_UPLOADED);
        }

        // Thumbnail không bắt buộc: FE upload lỗi thì bỏ key, không làm hỏng video
        if (lecture.getThumbnailKey() != null && !storageService.exist(BucketType.PUBLIC, lecture.getThumbnailKey())) {
            lecture.setThumbnailKey(null);
        }

        // Hiện chưa cắt chunk: dùng thẳng file gốc. Sau này đổi thành PROCESSING và đẩy job.
        lecture.setVideoKey(lecture.getVideoRawKey());
        lecture.setVideoDurationSeconds(duration);
        lecture.setVideoStatus(LectureVideoStatus.READY);
    }


}
