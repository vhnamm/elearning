package com.hnv.elearning.feature.course.dto.privates;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseCurriculumInstructorResponse {
    private Long courseVersion;
    private List<SectionInstructorResponse> sections;
}
