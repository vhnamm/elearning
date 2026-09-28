package com.hnv.elearning.feature.course.mapper;

import com.hnv.elearning.feature.course.dto.*;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.entity.CourseLearningOutcome;
import com.hnv.elearning.feature.course.entity.CourseRequiredSkill;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface CourseMapper {
    Course toEntity(CourseDraftRequest courseDraftRequest);
    CourseDraftDto toDto(Course course);

    CourseBasicInfoResponse toBasicInfoResponse(Course course);
    CourseLearningOutcomeResponse toOutcomeResponse(CourseLearningOutcome courseLearningOutcome);
    CourseRequiredSkillResponse toRequiredSkillResponse(CourseRequiredSkill courseRequiredSkill);

}
