package com.hnv.elearning.feature.review.service;

import java.util.Collection;
import java.util.Map;

public interface CourseReviewService {
    // Điểm trung bình + số review trong một truy vấn.
    Map<Long, RatingSummary> getRatingSummariesByCourseIds(Collection<Long> courseIds);

    record RatingSummary(double averageRating, long reviewCount) {
        public static final RatingSummary EMPTY = new RatingSummary(0.0, 0L);
    }
}
