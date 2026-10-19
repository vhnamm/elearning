package com.hnv.elearning.infrastructure.storage;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadResponse {
    private String uploadUrl;
    private String objectKey;
}
