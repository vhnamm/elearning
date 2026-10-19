package com.hnv.elearning.feature.course.dto.instructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDraftResponse {
    private Long id;
    private Long versionId;
    private Integer versionNo;
    private String title;
    private String slug;

}
