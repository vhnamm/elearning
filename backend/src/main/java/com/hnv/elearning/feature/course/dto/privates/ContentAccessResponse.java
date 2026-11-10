package com.hnv.elearning.feature.course.dto.privates;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentAccessResponse {
    private String accessUrl;
    private Instant expiresAt;
    private String contentType;
    private String mimeType;
    private String fileName;
    private Integer durationSeconds;
}
