package com.hnv.elearning.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCurriculumResponse {
    private Integer totalDurationSeconds;
    private Integer totalQuizzes;
    private List<SectionDto> sections;
}
