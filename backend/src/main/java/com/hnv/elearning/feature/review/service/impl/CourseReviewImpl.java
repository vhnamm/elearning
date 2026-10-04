package com.hnv.elearning.feature.review.service.impl;

import com.hnv.elearning.feature.review.repository.CourseReviewRepository;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseReviewImpl implements CourseReviewService {
    private final CourseReviewRepository courseReviewRepository;

    @Override
    public Map<Long, RatingSummary> getRatingSummariesByCourseIds(Collection<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }
        return courseReviewRepository.getRatingSummariesByCourseIds(courseIds).stream().collect(Collectors.toMap(
                item -> (Long) item[0],
                item -> new RatingSummary(((Number) item[1]).doubleValue(), ((Number) item[2]).longValue())
        ));
    }
}
