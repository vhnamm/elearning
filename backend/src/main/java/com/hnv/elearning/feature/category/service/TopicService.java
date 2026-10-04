package com.hnv.elearning.feature.category.service;

import com.hnv.elearning.feature.category.dto.TopicDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TopicService {
    public List<TopicDto> searchTopics(String keyword);
    List<TopicDto> getTrendingTopics();
}
