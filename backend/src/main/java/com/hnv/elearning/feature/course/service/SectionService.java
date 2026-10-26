package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.instructor.SectionCreateRequest;
import com.hnv.elearning.feature.course.dto.instructor.SectionCreateResponse;
import com.hnv.elearning.feature.course.dto.instructor.SectionReorderRequest;

public interface SectionService {
    SectionCreateResponse createSection(SectionCreateRequest sectionCreateRequest, Long courseId, Long versionId, Long instructorId);

    void deleteSection(Long courseId, Long versionId, Long sectionId, Long instructorId);

    void reorderSection(Long courseId, Long versionId, Long instructorId, SectionReorderRequest request);
}
