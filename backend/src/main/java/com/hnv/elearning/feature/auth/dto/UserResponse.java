package com.hnv.elearning.feature.auth.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String email;
    private String fullName;
    private String avatar;
    private List<String> roles;
}
