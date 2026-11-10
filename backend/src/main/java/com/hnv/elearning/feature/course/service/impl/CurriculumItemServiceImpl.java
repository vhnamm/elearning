package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.dto.privates.*;
import com.hnv.elearning.feature.course.entity.CurriculumItem;
import com.hnv.elearning.feature.course.entity.Lecture;
import com.hnv.elearning.feature.course.entity.Quiz;
import com.hnv.elearning.feature.course.entity.Section;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import com.hnv.elearning.feature.course.repository.CurriculumItemRepository;
import com.hnv.elearning.feature.course.repository.LectureRepository;
import com.hnv.elearning.feature.course.repository.SectionRepository;
import com.hnv.elearning.feature.course.service.CurriculumItemService;
import com.hnv.elearning.feature.course.service.LectureContentHandler;
import com.hnv.elearning.infrastructure.storage.BucketType;
import com.hnv.elearning.infrastructure.storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurriculumItemServiceImpl implements CurriculumItemService {
    private static final int DEFAULT_PASSING_SCORE = 70;

    private final CurriculumItemRepository curriculumItemRepository;
    private final SectionRepository sectionRepository;
    private final LectureContentHandlerRegistry handlerRegistry;
    private final StorageService storageService;
    private final LectureRepository lectureRepository;

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
            item.setQuiz(Quiz.builder()
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

        List<String> privateKeys = new ArrayList<>();
        String thumbnailKey = null;

        Lecture lecture = item.getLecture();
        if(lecture != null) {
            privateKeys = Stream.of(lecture.getVideoKey(), lecture.getVideoRawKey(), lecture.getFileKey()).filter(Objects::nonNull).distinct().toList();
            thumbnailKey = lecture.getThumbnailKey();
        }
        curriculumItemRepository.delete(item);
        curriculumItemRepository.shiftPositionAfterDelete(sectionId, position);
        curriculumItemRepository.flush();

        // Chỉ xoá file mà không còn lecture nào khác (vd: version đã publish) đang dùng
        List<String> keysToDelete = privateKeys.stream().filter(
                key -> !lectureRepository.existsByAnyKey(key)
        ).toList();
        String thumbToDelete = (thumbnailKey != null && !lectureRepository.existsByAnyKey(thumbnailKey))
                ? thumbnailKey : null;

        // Xoá file trên R2 SAU KHI DB commit thành công: nếu DB rollback thì file vẫn còn nguyên.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                keysToDelete.forEach(key -> deleteFileQuietly(BucketType.PRIVATE, key));
                if (thumbToDelete != null) {
                    deleteFileQuietly(BucketType.PUBLIC, thumbToDelete);
                }
            }
        });
    }

    // DB đã xoá xong nên lỗi R2 không được làm hỏng request, chỉ ghi log để dọn file thừa sau.
    private void deleteFileQuietly(BucketType bucket, String key) {
        try {
            storageService.delete(bucket, key);
            log.info("Đã xoá file R2: bucket={}, key={}", bucket, key);
        } catch (Exception e) {
            log.warn("Không xoá được file R2 (file thừa, cần dọn thủ công): bucket={}, key={}", bucket, key, e);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public void updateItem(Long itemId, Long instructorId, CurriculumItemUpdateRequest request) {
        CurriculumItem item = curriculumItemRepository.findOwnedItemById(itemId, instructorId).orElseThrow(
                () -> new AppException(ErrorCode.CURRICULUM_ITEM_NOT_EXIST)
        );
        CourseVersionStatus status = item.getSection().getCourseVersion().getStatus();
        if (status != CourseVersionStatus.DRAFT && status != CourseVersionStatus.REJECTED) {
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            item.setTitle(title);
        }

        if (request.getPreview() != null) {
            // Chỉ bài giảng mới cho xem thử, quiz thì không
            if (item.getType() != CurriculumItemType.LECTURE) {
                throw new AppException(ErrorCode.CURRICULUM_ITEM_NOT_LECTURE);
            }
            item.setPreview(request.getPreview());
        }
        // Không cần save(): item đang được quản lý trong @Transactional, Hibernate tự ghi khi commit.
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

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public ContentAccessResponse getContentAccess(Long itemId, Long instructorId) {
        CurriculumItem item = curriculumItemRepository.findOwnedItemById(itemId, instructorId).orElseThrow(
                () -> new AppException(ErrorCode.CURRICULUM_ITEM_NOT_EXIST)
        );
        LectureContentHandler handler = handlerRegistry.getHandler(item.getLecture().getContentType());
        return handler.createAccess(item);
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
