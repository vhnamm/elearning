package com.hnv.elearning.feature.category.mapper;

import com.hnv.elearning.feature.category.dto.TopicDto;
import com.hnv.elearning.feature.category.entity.Topic;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    TopicDto toTopicDto(Topic topic);
}
