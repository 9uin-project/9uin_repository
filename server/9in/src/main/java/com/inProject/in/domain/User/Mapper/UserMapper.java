package com.inProject.in.domain.User.Mapper;

import com.inProject.in.domain.User.Dto.RequestUserDto;
import com.inProject.in.domain.User.Dto.ResponseUserDto;
import com.inProject.in.domain.User.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    User toEntity(RequestUserDto requestUserDto);

    ResponseUserDto toDto(User user);
}
