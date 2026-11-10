package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.privates.SectionCreateRequest;
import com.hnv.elearning.feature.course.dto.privates.SectionCreateResponse;
import com.hnv.elearning.feature.course.dto.privates.SectionReorderRequest;
import com.hnv.elearning.feature.course.dto.privates.SectionUpdateRequest;

public interface SectionService {
    SectionCreateResponse createSection(SectionCreateRequest sectionCreateRequest, Long courseId, Long versionId, Long instructorId);

    void deleteSection(Long courseId, Long versionId, Long sectionId, Long instructorId);

    void updateSection(Long courseId, Long versionId, Long sectionId, Long instructorId, SectionUpdateRequest request);

    void reorderSection(Long courseId, Long versionId, Long instructorId, SectionReorderRequest request);
}
