package com.hnv.elearning.feature.course.dto.privates;

import lombok.Getter;
import lombok.Setter;

// PATCH: trường nào null thì giữ nguyên.
@Getter
@Setter
public class SectionUpdateRequest {
    private String title;
}
