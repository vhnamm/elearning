package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.dto.instructor.ContentCompleteRequest;
import com.hnv.elearning.feature.course.dto.instructor.ContentPresignedUploadResponse;
import com.hnv.elearning.feature.course.dto.instructor.ContentUploadRequest;
import com.hnv.elearning.feature.course.dto.instructor.CurriculumItemCreateRequest;
import com.hnv.elearning.feature.course.dto.instructor.CurriculumItemCreateResponse;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.entity.Lecture;
import com.hnv.elearning.feature.course.entity.Quizz;
import com.hnv.elearning.feature.course.entity.Section;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import com.hnv.elearning.feature.course.repository.CurriculumItemRepository;
import com.hnv.elearning.feature.course.repository.SectionRepository;
import com.hnv.elearning.feature.course.service.CurriculumItemService;
import com.hnv.elearning.feature.course.service.LectureContentHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurriculumItemServiceImpl implements CurriculumItemService {
    private static final int DEFAULT_PASSING_SCORE = 70;

    private final CurriculumItemRepository curriculumItemRepository;
    private final SectionRepository sectionRepository;
    private final LectureContentHandlerRegistry handlerRegistry;


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public CurriculumItemCreateResponse createItem(Long sectionId, Long instructorId, CurriculumItemCreateRequest request) {

        Section section = findEditableSection(sectionId, instructorId);

        CurriculumItem item = CurriculumItem.builder()
                .section(section)
                .type(request.getType())
                .title(request.getTitle().trim())
                .position(curriculumItemRepository.findMaxPosition(sectionId) + 1)
                .build();

        // contentType/videoStatus của Lecture để null, chỉ có khi giảng viên chọn nội dung (VIDEO/DOCUMENT)
        if (request.getType() == CurriculumItemType.LECTURE) {
            item.setLecture(Lecture.builder()
                    .curriculumItem(item)
                    .build());
        } else {
            item.setQuizz(Quizz.builder()
                    .curriculumItem(item)
                    .passingScore(DEFAULT_PASSING_SCORE)
                    .build());
        }

        CurriculumItem saved = curriculumItemRepository.save(item);
        return new CurriculumItemCreateResponse(saved.getId(), saved.getType(), saved.getTitle(), saved.getPosition());
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public void deleteItem(Long sectionId, Long instructorId, Long itemId) {
        CurriculumItem item = curriculumItemRepository.findOwnedItem(sectionId, itemId, instructorId).orElseThrow(
                () -> new AppException(ErrorCode.CURRICULUM_ITEM_NOT_EXIST)
        );
        CourseVersionStatus status = item.getSection().getCourseVersion().getStatus();
        if( status != CourseVersionStatus.DRAFT && status != CourseVersionStatus.REJECTED ) {
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }
        int position = item.getPosition();
        curriculumItemRepository.delete(item);
        curriculumItemRepository.shiftPositionAfterDelete(sectionId, position);
        //Sau khi làm bước tải bài giảng lên R2 thì làm thêm ở đây bước xoá trên R2
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public ContentPresignedUploadResponse getPresignedContentUpload(Long itemId, Long instructorId, ContentUploadRequest request) {
        CurriculumItem item = findEditableLectureItem(itemId, instructorId);
        LectureContentHandler handler = handlerRegistry.getHandler(request.getKind());
        return handler.prepareUpload(item, request);
    }

    @Transactional
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public void completeContentUpload(ContentCompleteRequest request, Long itemId, Long instructorId) {
        CurriculumItem item = findEditableLectureItem(itemId, instructorId);
        LectureContentHandler handler = handlerRegistry.getHandler(item.getLecture().getContentType());
        handler.completeUpload(item, request);
    }

    private Section findEditableSection(Long sectionId, Long instructorId) {
        Section section = sectionRepository.findOwnedSection(sectionId, instructorId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_EXIST));
        CourseVersionStatus status = section.getCourseVersion().getStatus();
        if (status != CourseVersionStatus.DRAFT && status != CourseVersionStatus.REJECTED) {
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }
        return section;
    }

    private CurriculumItem findEditableLectureItem(Long itemId, Long instructorId) {
        CurriculumItem item = curriculumItemRepository.findOwnedItemById(itemId, instructorId)
                .orElseThrow(() -> new AppException(ErrorCode.CURRICULUM_ITEM_NOT_EXIST));
        CourseVersionStatus status = item.getSection().getCourseVersion().getStatus();
        if (status != CourseVersionStatus.DRAFT && status != CourseVersionStatus.REJECTED) {
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }
        if (item.getType() != CurriculumItemType.LECTURE) {
            throw new AppException(ErrorCode.CURRICULUM_ITEM_NOT_LECTURE);
        }
        return item;
    }
}
