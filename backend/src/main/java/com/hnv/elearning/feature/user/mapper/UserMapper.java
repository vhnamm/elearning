package com.hnv.elearning.feature.user.mapper;

import com.hnv.elearning.feature.user.dto.InstructorInfoDto;
import com.hnv.elearning.feature.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    InstructorInfoDto toInstructorInfoDto(User user);
}
