package com.hnv.elearning.feature.review.service.impl;

import com.hnv.elearning.feature.review.repository.CourseReviewRepository;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseReviewImpl implements CourseReviewService {
    private final CourseReviewRepository courseReviewRepository;

    @Override
    public Map<Long, Double> getAverageRatingsByCourseIds(Collection<Long> courseIds){
        List<Object[]> list =  courseReviewRepository.getAverageRatingsByCourseIds(courseIds);

        return list.stream().collect(Collectors.toMap(
                item -> (Long) item[0],
                item -> ((Number) item[1]).doubleValue()
        ));

    }
}
