package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.dto.instructor.SectionCreateRequest;
import com.hnv.elearning.feature.course.entity.CourseVersion;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import com.hnv.elearning.feature.course.dto.instructor.SectionCreateResponse;
import com.hnv.elearning.feature.course.entity.Section;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.repository.CourseVersionRepository;
import com.hnv.elearning.feature.course.repository.SectionRepository;
import com.hnv.elearning.feature.course.service.SectionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;
    private final CourseVersionRepository courseVersionRepository;
    private final CourseRepository courseRepository;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public SectionCreateResponse createSection(SectionCreateRequest sectionCreateRequest, Long courseId, Long versionId, Long instructorId) {
        CourseVersion version = assertOwnsVersion(courseId, versionId, instructorId);

        Section section = Section.builder()
                .title(sectionCreateRequest.getTitle())
                .courseVersion(version)
                .position(sectionRepository.findMaxPosition(versionId) + 1)
                .build();

        Section savedSection = sectionRepository.save(section);

        return new SectionCreateResponse(savedSection.getId(), savedSection.getTitle(), savedSection.getPosition());
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public void deleteSection(Long courseId, Long versionId, Long sectionId, Long instructorId) {
        assertOwnsVersion(courseId, versionId, instructorId);
        Section section = sectionRepository.findByIdAndCourseVersionId(versionId, sectionId).orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_EXIST));

        int positionToShift = section.getPosition();
        sectionRepository.delete(section);
        sectionRepository.shiftPositionAfterDelete(versionId, positionToShift);
    }

    private CourseVersion assertOwnsVersion(Long courseId, Long versionId, Long instructorId) {
        if (!courseRepository.existsByIdAndInstructorId(courseId, instructorId)) {
            throw new AppException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        CourseVersion version = courseVersionRepository.findByIdAndCourseId(versionId, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_VERSION_NOT_EXIST));
        if (version.getStatus() != CourseVersionStatus.DRAFT && version.getStatus() != CourseVersionStatus.REJECTED) {
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }
        return version;
    }
}
