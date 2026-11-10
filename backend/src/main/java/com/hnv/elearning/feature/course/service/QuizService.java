package com.hnv.elearning.feature.course.service;


import com.hnv.elearning.feature.course.dto.privates.QuestionCreateRequest;
import com.hnv.elearning.feature.course.dto.privates.QuestionCreateResponse;
import com.hnv.elearning.feature.course.dto.privates.QuestionUpdateRequest;
import com.hnv.elearning.feature.course.dto.privates.QuizQuestionsResponse;

public interface QuizService {
    QuestionCreateResponse createQuestion(QuestionCreateRequest questionCreateRequest, Long itemId, Long instructorId);
    QuizQuestionsResponse getQuizQuestions(Long itemId, Long instructorId);
    QuestionCreateResponse updateQuestion(QuestionUpdateRequest request, Long itemId, Long questionId, Long instructorId);
    void deleteQuestion(Long itemId, Long questionId, Long instructorId);
}
