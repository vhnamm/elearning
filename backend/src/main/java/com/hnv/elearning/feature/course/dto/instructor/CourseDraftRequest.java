package com.hnv.elearning.feature.course.dto.instructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDraftRequest {
    private String title;
    private Long categoryId;
}
