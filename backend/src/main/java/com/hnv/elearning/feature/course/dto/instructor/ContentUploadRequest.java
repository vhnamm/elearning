package com.hnv.elearning.feature.course.dto.instructor;

import com.hnv.elearning.feature.course.enums.LectureType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentUploadRequest {
    private LectureType kind;
    private String contentType;
    private String fileName;
    private Long fileSizeBytes;
    // chỉ dùng cho VIDEO, null = không upload thumbnail
    private String thumbnailContentType;

}
