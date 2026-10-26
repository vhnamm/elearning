package com.hnv.elearning.feature.course.dto.instructor;

import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CurriculumItemCreateResponse {
    private Long id;
    private CurriculumItemType type;
    private String title;
    private int position;
}
