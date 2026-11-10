package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.privates.QuestionCreateRequest;
import com.hnv.elearning.feature.course.dto.privates.QuestionCreateResponse;
import com.hnv.elearning.feature.course.dto.privates.QuestionUpdateRequest;
import com.hnv.elearning.feature.course.dto.privates.QuizQuestionsResponse;
import com.hnv.elearning.feature.course.service.QuizService;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping("/curriculum-items/{itemId}/questions")
    public ResponseEntity<ApiResponse<QuestionCreateResponse>> createQuestion(
            @PathVariable Long itemId,
            @AuthenticationPrincipal User user,
            @RequestBody QuestionCreateRequest request
    ){
        ApiResponse response = ApiResponse.builder()
                .data(quizService.createQuestion(request, itemId, user.getId()))
                .code(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/curriculum-items/{itemId}/questions")
    public ResponseEntity<ApiResponse<QuizQuestionsResponse>> getQuestions(
            @PathVariable Long itemId,
            @AuthenticationPrincipal User user
    ){
        ApiResponse response = ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .data(quizService.getQuizQuestions(itemId, user.getId()))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/curriculum-items/{itemId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionCreateResponse>> updateQuestion(
            @PathVariable("itemId") Long itemId,
            @PathVariable("questionId") Long questionId,
            @AuthenticationPrincipal User user,
            @RequestBody QuestionUpdateRequest request

    ){
        ApiResponse response = ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .data(quizService.updateQuestion(request, itemId, questionId, user.getId()))
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/curriculum-items/{itemId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @AuthenticationPrincipal User user,
            @PathVariable("itemId") Long itemId,
            @PathVariable("questionId") Long questionId
    ){
        quizService.deleteQuestion(itemId, questionId, user.getId());

        ApiResponse response = ApiResponse.builder()
                .code(HttpStatus.NO_CONTENT.value())
                .build();

        return  ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
