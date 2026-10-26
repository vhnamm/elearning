package com.hnv.elearning.feature.course.dto.instructor;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPresignedUploadResponse {
    private String uploadUrl;
    private String thumbnailUploadUrl;
    private String objectKey;
    private String thumbnailKey;
}
