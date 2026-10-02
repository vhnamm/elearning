package com.hnv.elearning.feature.moderation.dto;

import com.hnv.elearning.feature.moderation.enums.RejectReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectCourseRequest {
    private RejectReason reasonCategory;
    private String feedback;
}
