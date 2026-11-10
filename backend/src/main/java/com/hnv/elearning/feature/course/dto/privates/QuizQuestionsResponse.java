package com.hnv.elearning.feature.course.dto.privates;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionsResponse {
    private Long quizId;
    private List<QuestionCreateResponse> questions;
}
