package com.hnv.elearning.feature.course.dto.privates;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionUpdateRequest {
    private Long id;
    private String content;
    private Boolean isCorrect;
}
