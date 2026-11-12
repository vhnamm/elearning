package com.hnv.elearning.feature.course.dto.privates;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoursePricingResponse {
    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal walletBalance;
}
