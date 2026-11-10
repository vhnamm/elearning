package com.hnv.elearning.feature.course.dto.privates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionUpdateRequest {
    private Long id;
    private String content;
    private List<OptionUpdateRequest> options;
}
