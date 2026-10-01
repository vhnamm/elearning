package com.hnv.elearning.feature.course.mapper;

import com.hnv.elearning.feature.course.dto.*;
import com.hnv.elearning.feature.course.entity.*;
import com.hnv.elearning.feature.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CourseMapper {
    Course toEntity(CourseDraftRequest courseDraftRequest);
    CourseDraftDto toDto(Course course);

    CourseBasicInfoResponse toBasicInfoResponse(Course course);
    CourseLearningOutcomeResponse toOutcomeResponse(CourseLearningOutcome courseLearningOutcome);
    CourseRequiredSkillResponse toRequiredSkillResponse(CourseRequiredSkill courseRequiredSkill);

    @Mapping(target = "averageStar", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "studentCount", ignore = true)
    @Mapping(target = "totalDurationSeconds", ignore = true)
    @Mapping(target = "totalQuizzes", ignore = true)
    @Mapping(source = "learningOutcomes", target = "learningOutcomes")
    @Mapping(source = "instructor", target = "instructor")
    CourseDetailDto toCourseDetailDto(Course course);

    CourseDetailDto.SectionDto toSectionDto(Section section);

    CourseDetailDto.InstructorInfoDto toInstructorInfoDto(User user);

    default CourseDetailDto.CurriculumItemDto toCurriculumItemDto(CurriculumItem entity) {
        if (entity == null) {
            return null;
        }

        CourseDetailDto.CurriculumItemDto dto = new CourseDetailDto.CurriculumItemDto();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setPosition(entity.getPosition());

        dto.setPreview(entity.isPreview());

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
}