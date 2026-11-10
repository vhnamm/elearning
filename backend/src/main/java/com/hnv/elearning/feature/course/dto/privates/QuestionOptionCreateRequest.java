package com.hnv.elearning.feature.course.dto.privates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionCreateRequest {
    private String content;
    private Boolean isCorrect;
}