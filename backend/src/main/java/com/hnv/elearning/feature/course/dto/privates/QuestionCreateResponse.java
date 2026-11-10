package com.hnv.elearning.feature.course.dto.privates;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionCreateResponse {
    private Long id;
    private String content;
    private Integer position;
    private List<OptionResponse> options;
}
