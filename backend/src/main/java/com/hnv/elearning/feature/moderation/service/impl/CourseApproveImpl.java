package com.hnv.elearning.feature.moderation.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.entity.Topic;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.entity.CourseLearningOutcome;
import com.hnv.elearning.feature.course.entity.CourseRequiredSkill;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.moderation.dto.ApproveCourseRequest;
import com.hnv.elearning.feature.moderation.dto.CourseReviewDetailResponse;
import com.hnv.elearning.feature.moderation.dto.ModerationResultResponse;
import com.hnv.elearning.feature.moderation.dto.PendingCourseItemDto;
import com.hnv.elearning.feature.moderation.dto.RejectCourseRequest;
import com.hnv.elearning.feature.moderation.dto.RejectReasonOptionDto;
import com.hnv.elearning.feature.moderation.entity.CourseModerationLog;
import com.hnv.elearning.feature.moderation.enums.ModerationAction;
import com.hnv.elearning.feature.moderation.enums.RejectReason;
import com.hnv.elearning.feature.moderation.repository.CourseModerationLogRepository;
import com.hnv.elearning.feature.moderation.service.CourseApproveService;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.repository.UserRepository;
import com.hnv.elearning.infrastructure.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseApproveImpl implements CourseApproveService {
    private static final int MIN_REJECT_FEEDBACK_LENGTH = 30;
    private static final String DEFAULT_APPROVE_FEEDBACK = "Khoá học đạt yêu cầu kiểm duyệt và được phê duyệt mở bán.";
    private static final String REJECT_MAIL_SUBJECT = "[MótEdu] Khoá học của bạn chưa được phê duyệt";
    private static final String REJECT_MAIL_TEMPLATE = "course_rejected";

    private final CourseRepository courseRepository;
    private final CourseModerationLogRepository moderationLogRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @Override
    public Page<PendingCourseItemDto> getPendingCourses(Pageable pageable) {
        return courseRepository.findByStatus(CourseStatus.PENDING_REVIEW, pageable)
                .map(this::toPendingItem);
    }

    @Override
    public long countPendingCourses() {
        return courseRepository.countByStatus(CourseStatus.PENDING_REVIEW);
    }

    @Override
    public CourseReviewDetailResponse getCourseForReview(Long courseId) {
        Course course = courseRepository.findForReviewById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return toReviewDetail(course);
    }

    @Override
    @Transactional
    public ModerationResultResponse approveCourse(Long courseId, ApproveCourseRequest request, Long moderatorId) {
        Course course = getPendingCourse(courseId);
        String feedback = request == null || request.getFeedback() == null || request.getFeedback().isBlank()
                ? DEFAULT_APPROVE_FEEDBACK
                : request.getFeedback().trim();

        course.setStatus(CourseStatus.PUBLISHED);
        CourseModerationLog moderationLog = saveLog(course, moderatorId, ModerationAction.APPROVED, feedback);

        return toResult(course, moderationLog);
    }

    @Override
    @Transactional
    public ModerationResultResponse rejectCourse(Long courseId, RejectCourseRequest request, Long moderatorId) {
        if (request == null || request.getReasonCategory() == null) {
            throw new AppException(ErrorCode.REJECT_REASON_REQUIRED);
        }
        String detail = request.getFeedback() == null ? "" : request.getFeedback().trim();
        if (detail.length() < MIN_REJECT_FEEDBACK_LENGTH) {
            throw new AppException(ErrorCode.REJECT_FEEDBACK_TOO_SHORT);
        }

        Course course = getPendingCourse(courseId);
        RejectReason reason = request.getReasonCategory();
        String feedback = "[" + reason.getLabel() + "] " + detail;

        course.setStatus(CourseStatus.REJECTED);
        CourseModerationLog moderationLog = saveLog(course, moderatorId, ModerationAction.REJECTED, feedback);

        sendRejectMail(course, reason, detail);
        return toResult(course, moderationLog);
    }

    @Override
    public List<RejectReasonOptionDto> getRejectReasons() {
        return Arrays.stream(RejectReason.values())
                .map(reason -> RejectReasonOptionDto.builder()
                        .value(reason.name())
                        .label(reason.getLabel())
                        .build())
                .toList();
    }

    private Course getPendingCourse(Long courseId) {
        Course course = courseRepository.findForReviewById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (course.getStatus() != CourseStatus.PENDING_REVIEW) {
            throw new AppException(ErrorCode.COURSE_NOT_PENDING_REVIEW);
        }
        return course;
    }

    private CourseModerationLog saveLog(Course course, Long moderatorId, ModerationAction action, String feedback) {
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CourseModerationLog moderationLog = CourseModerationLog.builder()
                .course(course)
                .moderator(moderator)
                .action(action)
                .feedback(feedback)
                .createdAt(LocalDateTime.now())
                .build();
        return moderationLogRepository.save(moderationLog);
    }

    // Gửi bất đồng bộ; lỗi gửi mail chỉ ghi log, không rollback quyết định kiểm duyệt.
    private void sendRejectMail(Course course, RejectReason reason, String detail) {
        User instructor = course.getInstructor();
        if (instructor == null || instructor.getEmail() == null) {
            return;
        }

        Context context = new Context();
        context.setVariable("instructorName", instructor.getFullName());
        context.setVariable("courseId", course.getId());
        context.setVariable("courseTitle", course.getTitle());
        context.setVariable("reasonLabel", reason.getLabel());
        context.setVariable("detail", detail);
        String body = templateEngine.process(REJECT_MAIL_TEMPLATE, context);

        emailService.sendHtmlEmail(instructor.getEmail(), REJECT_MAIL_SUBJECT, body).whenComplete(
                (result, throwable) -> {
                    if (throwable != null) {
                        log.error("Gửi mail từ chối khoá học {} thất bại: {}", course.getId(), throwable.getMessage());
                    }
                });
    }

    private PendingCourseItemDto toPendingItem(Course course) {
        User instructor = course.getInstructor();
        return PendingCourseItemDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .categoryName(resolveCategoryName(course))
                .subcategoryName(course.getSubcategory() != null ? course.getSubcategory().getName() : null)
                .price(course.getPrice())
                .level(course.getLevel())
                .instructorId(instructor != null ? instructor.getId() : null)
                .instructorName(instructor != null ? instructor.getFullName() : null)
                .instructorAvatar(instructor != null ? instructor.getAvatar() : null)
                .submittedAt(resolveSubmittedAt(course))
                .build();
    }

    private CourseReviewDetailResponse toReviewDetail(Course course) {
        User instructor = course.getInstructor();
        CourseReviewDetailResponse.InstructorInfo instructorInfo = instructor == null ? null
                : CourseReviewDetailResponse.InstructorInfo.builder()
                        .id(instructor.getId())
                        .fullName(instructor.getFullName())
                        .email(instructor.getEmail())
                        .avatar(instructor.getAvatar())
                        .build();

        return CourseReviewDetailResponse.builder()
                .id(course.getId())
                .slug(course.getSlug())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .level(course.getLevel())
                .status(course.getStatus())
                .categoryName(resolveCategoryName(course))
                .subcategoryName(course.getSubcategory() != null ? course.getSubcategory().getName() : null)
                .topics(course.getTopics().stream().map(Topic::getName).sorted().toList())
                .learningOutcomes(course.getLearningOutcomes().stream()
                        .sorted(Comparator.comparing(CourseLearningOutcome::getId))
                        .map(CourseLearningOutcome::getContent)
                        .toList())
                .requiredSkills(course.getRequiredSkills().stream()
                        .sorted(Comparator.comparing(CourseRequiredSkill::getId))
                        .map(CourseRequiredSkill::getContent)
                        .toList())
                .instructor(instructorInfo)
                .createdAt(course.getCreatedAt())
                .submittedAt(resolveSubmittedAt(course))
                .build();
    }

    private ModerationResultResponse toResult(Course course, CourseModerationLog moderationLog) {
        return ModerationResultResponse.builder()
                .courseId(course.getId())
                .status(course.getStatus())
                .action(moderationLog.getAction())
                .feedback(moderationLog.getFeedback())
                .moderatedAt(moderationLog.getCreatedAt())
                .build();
    }

    private String resolveCategoryName(Course course) {
        Category category = course.getCategory();
        if (category == null && course.getSubcategory() != null) {
            category = course.getSubcategory().getCategory();
        }
        return category != null ? category.getName() : null;
    }

    // Khoá chuyển sang PENDING_REVIEW qua một lần cập nhật nên updatedAt là thời điểm gửi duyệt gần nhất.
    private LocalDateTime resolveSubmittedAt(Course course) {
        return course.getUpdatedAt() != null ? course.getUpdatedAt() : course.getCreatedAt();
    }
}
