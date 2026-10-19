package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.feature.course.dto.instructor.CourseCurriculumInstructorResponse;
import com.hnv.elearning.feature.course.dto.instructor.CurriculumItemInstructorResponse;
import com.hnv.elearning.feature.course.dto.instructor.SectionInstructorResponse;
import com.hnv.elearning.feature.course.dto.publics.CourseCurriculumResponse;
import com.hnv.elearning.feature.course.dto.publics.SectionDto;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.entity.Quizz;
import com.hnv.elearning.feature.course.entity.Section;
import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import com.hnv.elearning.feature.course.enums.LectureType;
import com.hnv.elearning.feature.course.mapper.CourseMapper;
import com.hnv.elearning.feature.course.repository.QuestionRepository;
import com.hnv.elearning.feature.course.repository.SectionRepository;
import com.hnv.elearning.feature.course.service.CourseCurriculumService;
import com.hnv.elearning.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseCurriculumServiceImpl implements CourseCurriculumService {
    private final SectionRepository sectionRepository;
    private final CourseMapper courseMapper;
    private final QuestionRepository questionRepository;
    private final StorageService storageService;

    @Override
    public CourseCurriculumResponse getCurriculum(Long versionId) {
        List<Section> sections = sectionRepository.findCurriculumByVersionId(versionId);

        List<CurriculumItem> items = sections.stream()
                .filter(section -> section.getCurriculumItems() != null)
                .flatMap(section -> section.getCurriculumItems().stream())
                .toList();

        int totalDurationSeconds = items.stream()
                .filter(item -> item.getType() == CurriculumItemType.LECTURE && item.getLecture() != null)
                .map(item -> item.getLecture().getVideoDurationSeconds())
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalQuizzes = (int) items.stream()
                .filter(item -> item.getType() == CurriculumItemType.QUIZ)
                .count();

        List<SectionDto> sectionDtos = sections.stream()
                .map(courseMapper::toSectionDto)
                .toList();

        return CourseCurriculumResponse.builder()
                .totalDurationSeconds(totalDurationSeconds)
                .totalQuizzes(totalQuizzes)
                .sections(sectionDtos)
                .build();
    }


    @Override
    public CourseCurriculumInstructorResponse getInstructorCurriculum(Long versionId) {
        List<Section> sections = sectionRepository.findCurriculumByVersionId(versionId);


        List<Long> quizIds = sections.stream()
                .filter(section -> section.getCurriculumItems() != null)
                .flatMap(section -> section.getCurriculumItems().stream())
                .filter(curriculumItem -> curriculumItem.getType() == CurriculumItemType.QUIZ && curriculumItem.getQuizz() != null)
                .map(curriculumItem -> curriculumItem.getQuizz().getId())
                .toList();

        Map<Long, Long> mapQuestionCount = quizIds.isEmpty() ? Map.of() : questionRepository.countByQuizIds(quizIds).stream()
                .collect(Collectors.toMap(
                        QuestionRepository.QuizQuestionCount::getQuizId,
                        q -> q.getTotal() == null ? 0 : q.getTotal()
                ));

        List<SectionInstructorResponse> sectionDtos = sections.stream()
                .map(section -> {
                    List<CurriculumItemInstructorResponse> items = section.getCurriculumItems() == null
                            ? List.of()
                            : section.getCurriculumItems().stream()
                            .map(item -> courseMapper.toCurriculumItemInstructorResponse(
                                    item, mapQuestionCount, storageService::publicUrl))
                            .toList();

                    return new SectionInstructorResponse(
                            section.getId(),
                            section.getTitle(),
                            section.getPosition(),
                            items);
                })
                .toList();

        return CourseCurriculumInstructorResponse.builder()
                .sections(sectionDtos)
                .build();
    }
}
