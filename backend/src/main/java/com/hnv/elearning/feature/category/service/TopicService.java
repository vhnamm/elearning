package com.hnv.elearning.feature.category.service;

import com.hnv.elearning.feature.category.dto.TopicDto;

import java.util.List;

public interface TopicService {
    public List<TopicDto> searchTopics(String keyword);
}
