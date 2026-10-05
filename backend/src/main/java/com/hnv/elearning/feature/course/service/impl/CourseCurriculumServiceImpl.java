package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.feature.course.dto.CourseCurriculumResponse;
import com.hnv.elearning.feature.course.dto.SectionDto;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.entity.Section;
import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import com.hnv.elearning.feature.course.mapper.CourseMapper;
import com.hnv.elearning.feature.course.repository.SectionRepository;
import com.hnv.elearning.feature.course.service.CourseCurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseCurriculumServiceImpl implements CourseCurriculumService {
    private final SectionRepository sectionRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseCurriculumResponse getCurriculum(Long courseId) {
        List<Section> sections = sectionRepository.findCurriculumByCourseId(courseId);

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
}
