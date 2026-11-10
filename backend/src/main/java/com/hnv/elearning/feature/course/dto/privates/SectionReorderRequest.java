package com.hnv.elearning.feature.course.dto.privates;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SectionReorderRequest {
    private List<Long> sectionIds;
}
