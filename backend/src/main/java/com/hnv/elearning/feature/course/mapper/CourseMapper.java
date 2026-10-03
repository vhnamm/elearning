package com.hnv.elearning.feature.course.mapper;

import com.hnv.elearning.feature.category.mapper.TopicMapper;
import com.hnv.elearning.feature.course.dto.*;
import com.hnv.elearning.feature.course.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = TopicMapper.class, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {
    Course toEntity(CourseDraftRequest courseDraftRequest);
    CourseDraftDto toDto(Course course);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "subcategory.id", target = "subcategoryId")
    CourseBasicInfoResponse toBasicInfoResponse(Course course);

    CourseLearningOutcomeResponse toOutcomeResponse(CourseLearningOutcome courseLearningOutcome);
    CourseRequiredSkillResponse toRequiredSkillResponse(CourseRequiredSkill courseRequiredSkill);

    @Mapping(target = "averageStar", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "studentCount", ignore = true)
    @Mapping(source = "learningOutcomes", target = "learningOutcomes")
    @Mapping(target = "instructor", ignore = true)
    CourseDetailDto toCourseDetailDto(Course course);

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
}