package com.hnv.elearning.feature.course.dto.instructor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SectionInstructorResponse {
    private Long id;
    private String title;
    private int position;
    private List<CurriculumItemInstructorResponse> curriculumItems;

}
