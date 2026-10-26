package com.hnv.elearning.infrastructure.storage;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class StorageKeyFactory {
    private Map<String, String> ALLOWED_TYPE = Map.of("image/png", "png", "image/jpeg", "jpeg", "image/jpg", "jpg", "image/webp", "webp");
    private Map<String, String> ALLOWED_VIDEO_TYPE = Map.of(
            "video/mp4", "mp4",
            "video/webm", "webm",
            "video/quicktime", "mov"
    );

    private Map<String, String> ALLOWED_DOCUMENT_TYPE = Map.of(
            "application/pdf", "pdf",
            "application/msword", "doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx",
            "application/vnd.ms-powerpoint", "ppt",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx",
            "text/plain", "txt",
            "text/markdown", "md"
    );


    public String courseThumbnailGenerate(Long courseId, String contentType){
        // Map.of ném NPE khi tra cứu bằng null, nên contentType thiếu phải trả lỗi 415 thay vì 500.
        String extension = contentType == null ? null : ALLOWED_TYPE.get(contentType);
        if(extension == null){
            throw new AppException(ErrorCode.IMAGE_TYPE_NOT_SUPPORTED);

        }

        return String.format("courses/%d/thumbnail/cover_%d.%s", courseId, System.currentTimeMillis(), extension);
    }

    public String lectureVideoRawKeyGenerate(Long courseId, String stableKey, String uploadId, String contentType){
        String extension = contentType == null ? null : ALLOWED_VIDEO_TYPE.get(contentType);
        if(extension == null){
            throw new AppException(ErrorCode.VIDEO_TYPE_NOT_SUPPORTED);
        }

        return String.format("courses/%d/lectures/%s/video/%s/raw.%s", courseId, stableKey, uploadId, extension);
    }

    // Thumbnail nằm ở bucket PUBLIC, tách khỏi folder video (bucket PRIVATE)
    public String lectureThumbnailKeyGenerate(Long courseId, String stableKey, String uploadId, String contentType){
        String extension = contentType == null ? null : ALLOWED_TYPE.get(contentType);
        if(extension == null){
            throw new AppException(ErrorCode.IMAGE_TYPE_NOT_SUPPORTED);
        }
        return String.format("courses/%d/lectures/%s/thumb/%s.%s", courseId, stableKey, uploadId, extension);
    }

    public String lectureDocumentKeyGenerate(Long courseId, String stableKey, String uploadId, String contentType){
        String extension = contentType == null ? null : ALLOWED_DOCUMENT_TYPE.get(contentType);
        if(extension == null){
            throw new AppException(ErrorCode.DOCUMENT_TYPE_NOT_SUPPORTED);
        }
        return String.format("courses/%d/lectures/%s/document/%s/file.%s", courseId, stableKey, uploadId, extension);
    }

    public String courseThumbnailPrefix(long courseId) {
        return "courses/%d/thumbnail/".formatted(courseId);
    }
}
