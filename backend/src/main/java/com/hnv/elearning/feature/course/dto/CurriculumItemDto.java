package com.hnv.elearning.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumItemDto {
    private Long id;
    private String title;
    private String type;
    private int position;
    private boolean isPreview;
    private String videoKey;
    private Integer videoDurationSeconds;
    private Integer passingScore;
}
