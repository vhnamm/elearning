package com.hnv.elearning.feature.category.service;

import com.hnv.elearning.feature.category.dto.TopicDto;
import com.hnv.elearning.feature.category.mapper.TopicMapper;
import com.hnv.elearning.feature.category.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {
    private static final int MAX_RESULTS = 20;

    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    @Override
    public List<TopicDto> searchTopics(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        return topicRepository.findByNameContainingIgnoreCase(normalizedKeyword, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(topicMapper::toTopicDto)
                .toList();
    }
}
