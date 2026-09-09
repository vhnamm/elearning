package com.hnv.elearning.feature.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingUserDto {
    private String email;
    private String passwordHash;
    private String fullName;
}
