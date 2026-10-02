package com.hnv.elearning.feature.course.mapper;

import com.hnv.elearning.feature.category.mapper.TopicMapper;
import com.hnv.elearning.feature.course.dto.*;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.entity.CourseLearningOutcome;
import com.hnv.elearning.feature.course.entity.CourseRequiredSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", uses = TopicMapper.class, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface CourseMapper {
    Course toEntity(CourseDraftRequest courseDraftRequest);
    CourseDraftDto toDto(Course course);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "subcategory.id", target = "subcategoryId")
    CourseBasicInfoResponse toBasicInfoResponse(Course course);

    CourseLearningOutcomeResponse toOutcomeResponse(CourseLearningOutcome courseLearningOutcome);
    CourseRequiredSkillResponse toRequiredSkillResponse(CourseRequiredSkill courseRequiredSkill);
}
