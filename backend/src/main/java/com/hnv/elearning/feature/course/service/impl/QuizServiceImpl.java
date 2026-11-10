package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.dto.privates.*;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.entity.Question;
import com.hnv.elearning.feature.course.entity.QuestionOption;
import com.hnv.elearning.feature.course.entity.Quiz;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import com.hnv.elearning.feature.course.repository.CurriculumItemRepository;
import com.hnv.elearning.feature.course.repository.QuestionRepository;
import com.hnv.elearning.feature.course.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final CurriculumItemRepository curriculumItemRepository;
    private final QuestionRepository questionRepository;

    @Override
    public QuestionCreateResponse createQuestion(QuestionCreateRequest questionCreateRequest , Long itemId, Long instructorId) {
        CurriculumItem curriculumItem = checkEditableQuestion(itemId, instructorId);
        Quiz quiz = curriculumItem.getQuiz();

        Integer pos = questionRepository.findMaxQuestionPositionByQuizId(quiz.getId());

        Question question = Question.builder()
                .quiz(quiz)
                .position(pos + 1)
                .content(questionCreateRequest.getContent().trim())
                .build();

        questionCreateRequest.getOptions().forEach(option -> {
            QuestionOption questionOption = QuestionOption.builder()
                    .isCorrect(option.getIsCorrect())
                    .content(option.getContent().trim())
                    .build();

            question.addOption(questionOption);

        });

        Question saved = questionRepository.save(question);
        return QuestionCreateResponse.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .position(saved.getPosition())
                .options(saved.getOptions().stream().map(
                    option -> new OptionResponse(option.getId(), option.getContent(),option.getPosition(), option.getIsCorrect())
                ).toList())
                .build();
    }

    @Override
    public QuizQuestionsResponse getQuizQuestions(Long itemId, Long instructorId) {
        CurriculumItem item = checkEditableQuestion(itemId, instructorId);
        Quiz quiz = item.getQuiz();
        List<Question> questions = questionRepository.findByQuizIdOrderByPositionAsc(quiz.getId());
        QuizQuestionsResponse response = QuizQuestionsResponse.builder()
                .quizId(quiz.getId())
                .questions(questions.stream().map(
                        question -> QuestionCreateResponse.builder()
                                .id(question.getId())
                                .position(question.getPosition())
                                .content(question.getContent())
                                .options(question.getOptions().stream().map(
                                        option -> new OptionResponse(option.getId(), option.getContent(),option.getPosition(), option.getIsCorrect())
                                ).toList())
                                .build()
                ).toList())
                .build();

        return response;
    }

    @Override
    @Transactional
    public QuestionCreateResponse updateQuestion(QuestionUpdateRequest request, Long itemId, Long questionId, Long instructorId) {
        CurriculumItem item = checkEditableQuestion(itemId, instructorId);
        Question question = questionRepository.findByIdWithAnser(questionId).orElseThrow(
                () -> new AppException(ErrorCode.QUESTION_NOT_FOUND)
        );
        // Câu hỏi phải thuộc quiz của item trên URL
        if (!question.getQuiz().getId().equals(item.getQuiz().getId())) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }

        question.setContent(request.getContent().trim());

        Set<Long> incomingIds = request.getOptions().stream().map(
                option -> option.getId()
        ).filter(Objects::nonNull).collect(Collectors.toSet());

        // Đáp án đang có trong DB mà request không gửi id lên thì xoá
        question.getOptions().removeIf(
                option -> !incomingIds.contains(option.getId())
        );
        // Xoá xuống DB trước, tránh vướng UNIQUE (question_id, position) khi dồn vị trí
        questionRepository.flush();

        Map<Long, QuestionOption> afterRemoveMap = question.getOptions().stream()
                .collect(Collectors.toMap(
                        option -> option.getId(),
                        option -> option
                ));

        for(int i=0; i<request.getOptions().size(); i++){
            OptionUpdateRequest optionRequest =  request.getOptions().get(i);
            //update lua chon moi
            if(optionRequest.getId() != null && afterRemoveMap.containsKey(optionRequest.getId())){
                QuestionOption existingOption = afterRemoveMap.get(optionRequest.getId());
                existingOption.setContent(optionRequest.getContent());
                existingOption.setIsCorrect(optionRequest.getIsCorrect());
                existingOption.setPosition(i);
            }else{
                QuestionOption newOption = QuestionOption.builder()
                        .content(optionRequest.getContent())
                        .isCorrect(optionRequest.getIsCorrect())
                        .position(i)
                        .build();

                question.addOption(newOption);
            }
        }

        return QuestionCreateResponse.builder()
                .id(question.getId())
                .content(question.getContent())
                .position(question.getPosition())
                .options(question.getOptions().stream().map(
                        option -> new OptionResponse(option.getId(), option.getContent(), option.getPosition(), option.getIsCorrect())
                ).toList())
                .build();
    }

    @Override
    @Transactional
    public void deleteQuestion(Long itemId, Long questionId, Long instructorId) {
        CurriculumItem item = checkEditableQuestion(itemId, instructorId);

        Quiz quiz = item.getQuiz();
        Question toRemove = quiz.getQuestions().stream().filter(question -> question.getId().equals(questionId)).findFirst().orElseThrow(
                () -> new AppException(ErrorCode.QUESTION_NOT_FOUND)
        );

        quiz.getQuestions().remove(toRemove);
        toRemove.setQuiz(null);

    }

    private CurriculumItem checkEditableQuestion(Long itemId, Long instructorId) {
        CurriculumItem curriculumItem = curriculumItemRepository.findOwnedItemById(itemId, instructorId).orElseThrow(
                () -> new AppException(ErrorCode.CURRICULUM_ITEM_NOT_EXIST)
        );
        CourseVersionStatus status = curriculumItem.getSection().getCourseVersion().getStatus();

        if(status == CourseVersionStatus.PUBLISHED || status == CourseVersionStatus.PENDING_REVIEW){
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }
        if(curriculumItem.getType() != CurriculumItemType.QUIZ){
            throw new AppException(ErrorCode.CURRICULUM_ITEM_NOT_QUIZ);
        }
        return curriculumItem;
    }
}
