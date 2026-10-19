package com.hnv.elearning.feature.course.mapper;

import com.hnv.elearning.feature.category.mapper.TopicMapper;
import com.hnv.elearning.feature.course.dto.instructor.*;
import com.hnv.elearning.feature.course.dto.publics.*;
import com.hnv.elearning.feature.course.entity.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Map;
import java.util.function.Function;

@Mapper(componentModel = "spring", uses = TopicMapper.class, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {
    // Nội dung khóa học nằm ở CourseVersion. Course (slug, price, instructor, status) do service tạo riêng.
    // Category gắn ở service vì request chỉ có categoryId.
    @Mapping(target = "id", ignore = true)
    CourseVersion toVersionEntity(CourseDraftRequest courseDraftRequest);

    // id trả về là id của khóa (FE dùng làm courseId trên URL), title lấy từ version.
    @Mapping(source = "course.id", target = "id")
    @Mapping(source = "course.slug", target = "slug")
    @Mapping(source = "id", target = "versionId")
    CourseDraftResponse toDraftResponse(CourseVersion version);

    // id = id khóa; versionId = bản đang đọc. thumbnailUrl service tự dựng từ thumbnailKey qua StorageService.
    @Mapping(source = "course.id", target = "id")
    @Mapping(source = "id", target = "versionId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "subcategory.id", target = "subcategoryId")
    @Mapping(target = "thumbnailUrl", ignore = true)
    CourseBasicInfoResponse toBasicInfoResponse(CourseVersion version);

    // Cập nhật bản nháp từ form: trường FE không gửi (null) thì giữ nguyên giá trị cũ, chỉ chép các trường đơn giản.
    // Ba trường dưới phải ignore: outcomes/skills khác kiểu (List<String> so với Set<entity>),
    // thumbnailKey phải qua kiểm tra ở service nên không được chép thẳng.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "learningOutcomes", ignore = true)
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "thumbnailKey", ignore = true)
    void updateBasicInfo(CourseBasicInfoUpdateRequest request, @MappingTarget CourseVersion version);

    CourseLearningOutcomeResponse toOutcomeResponse(CourseLearningOutcome courseLearningOutcome);
    CourseRequiredSkillResponse toRequiredSkillResponse(CourseRequiredSkill courseRequiredSkill);

    @Mapping(target = "averageStar", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "studentCount", ignore = true)
    @Mapping(source = "course.id", target = "id")
    @Mapping(source = "course.price", target = "price")
    @Mapping(source = "course.status", target = "status")
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "instructor", ignore = true)
    CourseDetailResponse toCourseDetailDto(CourseVersion version);

    SectionDto toSectionDto(Section section);

    default CurriculumItemDto toCurriculumItemDto(CurriculumItem entity) {
        if (entity == null) {
            return null;
        }

        CurriculumItemDto dto = new CurriculumItemDto();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setPosition(entity.getPosition());

        dto.setPreview(entity.isPreview());

        if (entity.getType() != null) {
            dto.setType(entity.getType().name());
        }

        if (entity.getLecture() != null) {
            dto.setVideoDurationSeconds(entity.getLecture().getVideoDurationSeconds());

            if (entity.isPreview()) {
                dto.setVideoKey(entity.getLecture().getVideoKey());
            }
        }

        if (entity.getQuizz() != null) {
            dto.setPassingScore(entity.getQuizz().getPassingScore());
        }

        return dto;
    }


    default CurriculumItemInstructorResponse toCurriculumItemInstructorResponse(
            CurriculumItem entity,
            Map<Long, Long> questionCounts,
            Function<String, String> thumbnailUrlBuilder
            ) {
        if (entity == null) {
            return null;
        }
        CurriculumItemInstructorResponse dto = new CurriculumItemInstructorResponse();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setPosition(entity.getPosition());
        dto.setPreview(entity.isPreview());

        dto.setType(entity.getType().name());

        Lecture lecture = entity.getLecture();
        if (lecture != null) {
            // contentType và videoStatus có thể null: lecture vừa tạo, chưa có nội dung.
            if (lecture.getContentType() != null) {
                dto.setContentType(lecture.getContentType().name());
            }
            if (lecture.getVideoStatus() != null) {
                dto.setVideoStatus(lecture.getVideoStatus().name());
            }
            dto.setVideoDurationSeconds(lecture.getVideoDurationSeconds());
            dto.setFileName(lecture.getFileName());
            dto.setFileSizeBytes(lecture.getFileSizeBytes());
            if (lecture.getThumbnailKey() != null) {
                dto.setThumbnailUrl(thumbnailUrlBuilder.apply(lecture.getThumbnailKey()));
            }
        }

        Quizz quizz = entity.getQuizz();
        if (quizz != null) {
            dto.setPassingScore(quizz.getPassingScore());
            dto.setQuestionCount(questionCounts.getOrDefault(quizz.getId(), 0L));
        }

        return dto;
    }
}