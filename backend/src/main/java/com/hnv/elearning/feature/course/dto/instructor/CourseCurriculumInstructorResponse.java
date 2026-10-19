package com.hnv.elearning.feature.course.dto.instructor;

import com.hnv.elearning.feature.course.dto.publics.SectionDto;
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
