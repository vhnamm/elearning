package com.hnv.elearning.feature.course.dto.privates;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionCreateRequest {
    private String content;
    private List<QuestionOptionCreateRequest> options;
}
