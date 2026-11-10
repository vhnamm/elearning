package com.hnv.elearning.feature.course.dto.privates;

import lombok.Getter;
import lombok.Setter;

// PATCH: trường nào null thì giữ nguyên, không sửa.
@Getter
@Setter
public class CurriculumItemUpdateRequest {
    private String title;
    private Boolean preview;
}
