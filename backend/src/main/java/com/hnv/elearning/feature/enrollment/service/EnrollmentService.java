package com.hnv.elearning.feature.enrollment.service;

import java.util.Collection;
import java.util.Map;

public interface EnrollmentService {
    Map<Long, Integer> getEnrolledCountByCourseIds(Collection<Long> courseIds);
}
