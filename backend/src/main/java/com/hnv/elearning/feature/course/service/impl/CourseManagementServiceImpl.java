package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.common.utils.StringUtil;
import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.entity.Subcategory;
import com.hnv.elearning.feature.category.entity.Topic;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import com.hnv.elearning.feature.category.repository.TopicRepository;
import com.hnv.elearning.feature.course.dto.instructor.*;
import com.hnv.elearning.feature.course.dto.publics.CourseSearchRequest;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.entity.CourseVersion;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import com.hnv.elearning.feature.course.mapper.CourseMapper;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.repository.CourseVersionRepository;
import com.hnv.elearning.feature.course.service.CourseCurriculumService;
import com.hnv.elearning.feature.course.service.CourseManagementService;
import com.hnv.elearning.feature.course.specification.CourseVersionSpecification;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.repository.UserRepository;
import com.hnv.elearning.infrastructure.storage.BucketType;
import com.hnv.elearning.infrastructure.storage.PresignedUploadResponse;
import com.hnv.elearning.infrastructure.storage.StorageKeyFactory;
import com.hnv.elearning.infrastructure.storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseManagementServiceImpl implements CourseManagementService {
    private final CourseRepository courseRepository;
    private final CourseCurriculumService courseCurriculumService;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService  courseReviewService;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;
    private final CategoryRepository categoryRepository;
    private final StorageKeyFactory storageKeyFactory;
    private final StorageService storageService;
    private final CourseVersionRepository courseVersionRepository;
    private final TopicRepository topicRepository;


    @Value("${r2.ttl.image-upload}")
    private Duration PRESIGNED_IMAGE_UPLOAD_TTTL;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public Page<InstructorCourseItemDto> getInstructedCourses(CourseSearchRequest request, Long instructorId, Pageable pageable) {
        Specification<CourseVersion> spec = CourseVersionSpecification.forInstructor(instructorId, request.getStatus(), request.getKeyword());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), toInstructorSort(pageable.getSort()));
        Page<CourseVersion> versions = courseVersionRepository.findAll(spec, sortedPageable);

        if(versions.isEmpty()){
            return Page.empty(pageable);
        }

        List<Long> courseIds = versions.stream().map(
                version -> version.getCourse().getId()
        ).distinct().toList();

        Map<Long, Integer> enrollmentCountMap = enrollmentService.getEnrolledCountByCourseIds(courseIds);
        Map<Long, CourseReviewService.RatingSummary> ratingMap = courseReviewService.getRatingSummariesByCourseIds(courseIds);

        return versions.map(
                version -> {
                    Course course = version.getCourse();
                    return InstructorCourseItemDto.builder()
                            .id(course.getId())
                            .versionId(version.getId())
                            .versionNo(version.getVersionNo())
                            .rating(ratingMap.containsKey(course.getId()) ? ratingMap.get(course.getId()).averageRating() : null)
                            .price(course.getPrice())
                            .status(version.getStatus())
                            .thumbnailUrl(version.getThumbnailKey() == null ? null : storageService.publicUrl(version.getThumbnailKey()))
                            .title(version.getTitle())
                            .totalEnrollments(enrollmentCountMap.get(course.getId()))
                            .build();
                }
        );

    }

    // FE chỉ sort theo ngày tạo hoặc giá, cả hai là của khóa (Course) chứ không phải của version.
    // Sort phụ course.id + versionNo giữ hai version của cùng một khóa đứng cạnh nhau và ổn định giữa các lần gọi.
    private Sort toInstructorSort(Sort requested) {
        Sort.Order first = requested.isSorted() ? requested.iterator().next() : null;
        String field = (first != null && "price".equals(first.getProperty())) ? "course.price" : "course.createdAt";
        Sort.Direction direction = first != null ? first.getDirection() : Sort.Direction.DESC;
        return Sort.by(
                new Sort.Order(direction, field),
                Sort.Order.desc("course.id"),
                Sort.Order.desc("versionNo")
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    @Override
    public CourseDraftResponse createDraft(CourseDraftRequest courseDraftRequest, User user){
        User instructor = userRepository.findById(user.getId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findById(courseDraftRequest.getCategoryId()).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Course course = Course.builder()
                .instructor(instructor)
                .status(CourseStatus.DRAFT)
                .slug(StringUtil.toSlug(courseDraftRequest.getTitle()))
                .build();

        Course savedCourse;

        try{
            savedCourse = courseRepository.saveAndFlush(course);
        }catch (DataIntegrityViolationException ex){
            throw new AppException(ErrorCode.COURSE_ALREADY_EXIST);
        }


        //tao moi 1 version
        CourseVersion version = courseMapper.toVersionEntity(courseDraftRequest);
        version.setCourse(savedCourse);
        version.setCategory(category);
        version.setStatus(CourseVersionStatus.DRAFT);
        version.setVersionNo(1);

        CourseVersion savedVersion = courseVersionRepository.save(version);

        return courseMapper.toDraftResponse(savedVersion);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public CourseBasicInfoResponse getCourseBasicInfo(Long courseId, Long instructorId, Long versionId) {
        assertOwnsVersion(courseId, versionId, instructorId);

        CourseVersion version = courseVersionRepository.findWithDetailByIdAndCourseId(versionId, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_VERSION_NOT_EXIST));


        CourseBasicInfoResponse response = courseMapper.toBasicInfoResponse(version);

        response.setThumbnailUrl(version.getThumbnailKey() == null ? null : storageService.publicUrl(version.getThumbnailKey()));

        return response;
    }


    // Kiểm tra quyền ở đây (CourseCurriculumService không kiểm tra), rồi mới đọc đề cương.
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public CourseCurriculumInstructorResponse getCurriculumInstructor(Long instructorId, Long courseId, Long versionId) {
        assertOwnsVersion(courseId, versionId, instructorId);
        return courseCurriculumService.getInstructorCurriculum(versionId);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public PresignedUploadResponse getPresignedUrl(CourseThumbnailPresignedRequest request, Long courseId, User user) {
        //check owner
        boolean owned = courseRepository.existsByIdAndInstructorId(courseId, user.getId());
        if(!owned){
            throw new AppException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        String contentType = request.getContentType();
        String objectKey = storageKeyFactory.courseThumbnailGenerate(courseId, contentType);

        PresignedUploadResponse response = storageService.presignedUpload(
                BucketType.PUBLIC,
                objectKey,
                contentType,
                PRESIGNED_IMAGE_UPLOAD_TTTL
        );

        return response;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public void updateCourseBasicInfo(Long courseId, Long versionId, CourseBasicInfoUpdateRequest request, User user){

        assertOwnsVersion(courseId, versionId, user.getId());

        CourseVersion version = courseVersionRepository.findWithDetailByIdAndCourseId(versionId, courseId).orElseThrow(
                () -> new AppException(ErrorCode.COURSE_VERSION_NOT_EXIST)
        );
        if(version.getStatus() != CourseVersionStatus.DRAFT &&  version.getStatus() != CourseVersionStatus.REJECTED){
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        version.setCategory(category);

        Subcategory subcategory = category.getSubcategories().stream().filter(sub -> sub.getId().equals(request.getSubcategoryId())).findFirst().orElseThrow(
                () -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)
        );

        version.setSubcategory(subcategory);

        courseMapper.updateBasicInfo(request, version);

        version.replaceOutcomes(request.getLearningOutcomes());
        version.replaceSkills(request.getRequiredSkills());

        String newKey = request.getThumbnailKey();
        if (newKey != null && !newKey.equals(version.getThumbnailKey())) {
            boolean validKey = newKey.startsWith(storageKeyFactory.courseThumbnailPrefix(courseId))
                    && !newKey.contains("..");
            if (!validKey || !storageService.exist(BucketType.PUBLIC, newKey)) {
                throw new AppException(ErrorCode.IMAGE_NOT_UPLOADED);
            }
            version.setThumbnailKey(newKey);
        }


        List<Topic> topics = topicRepository.findAllByIds(request.getTopicIds());
        version.getTopics().clear();
        version.getTopics().addAll(topics);

        courseVersionRepository.save(version);
    }


    private void assertOwnsVersion(Long courseId, Long versionId, Long instructorId) {
        if (!courseRepository.existsByIdAndInstructorId(courseId, instructorId)) {
            throw new AppException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        if (!courseVersionRepository.existsByIdAndCourseId(versionId, courseId)) {
            throw new AppException(ErrorCode.COURSE_VERSION_NOT_EXIST);
        }
    }
}
