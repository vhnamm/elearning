package com.hnv.elearning.feature.review.service;

import java.util.Collection;
import java.util.Map;

public interface CourseReviewService {
    Map<Long, Double> getAverageRatingsByCourseIds(Collection<Long> courseIds);
}
