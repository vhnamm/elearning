package com.hnv.elearning.feature.course.dto.privates;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionResponse {
    private Long id;
    private String content;
    private Integer position;
    private Boolean isCorrect;
}
