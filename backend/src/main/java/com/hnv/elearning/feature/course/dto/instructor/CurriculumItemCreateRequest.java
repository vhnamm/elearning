package com.hnv.elearning.feature.course.dto.instructor;

import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurriculumItemCreateRequest {
    private CurriculumItemType type;
    private String title;
}
