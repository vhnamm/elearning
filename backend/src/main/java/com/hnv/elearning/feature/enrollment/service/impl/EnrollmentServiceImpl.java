package com.hnv.elearning.feature.enrollment.service.impl;

import com.hnv.elearning.feature.enrollment.repository.EnrollmentRepository;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Map<Long, Integer> getEnrolledCountByCourseIds(Collection<Long> courseIds){
        List<Object[]> list = enrollmentRepository.countEnrolledByCourseIds(courseIds);

        return list.stream().collect(Collectors.toMap(
                item -> (Long) item[0],
                item -> ((Number) item[1]).intValue()
        ));

    }
}
