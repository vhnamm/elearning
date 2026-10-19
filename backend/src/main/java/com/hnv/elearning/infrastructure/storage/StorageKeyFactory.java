package com.hnv.elearning.infrastructure.storage;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class StorageKeyFactory {
    private Map<String, String> ALLOWED_TYPE = Map.of("image/png", "png", "image/jpeg", "jpeg", "image/jpg", "jpg", "image/webp", "webp");

    public String courseThumbnailGenerate(Long courseId, String contentType){
        // Map.of ném NPE khi tra cứu bằng null, nên contentType thiếu phải trả lỗi 415 thay vì 500.
        String extension = contentType == null ? null : ALLOWED_TYPE.get(contentType);
        if(extension == null){
            throw new AppException(ErrorCode.IMAGE_TYPE_NOT_SUPPORTED);

        }

        return String.format("courses/%d/thumbnail/cover_%d.%s", courseId, System.currentTimeMillis(), extension);
    }

    public String courseThumbnailPrefix(long courseId) {
        return "courses/%d/thumbnail/".formatted(courseId);
    }
}
