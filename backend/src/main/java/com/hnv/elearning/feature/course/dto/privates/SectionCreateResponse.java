package com.hnv.elearning.feature.course.dto.privates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SectionCreateResponse {
    private Long id;
    private String title;
    private int position;
}
