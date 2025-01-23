package com.example.festimo.domain.admin.mapper;

import com.example.festimo.domain.admin.dto.AdminUpdateUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.example.festimo.domain.admin.dto.AdminDTO;
import com.example.festimo.domain.user.domain.User;

@Mapper
public interface AdminMapper {

    AdminMapper INSTANCE = Mappers.getMapper(AdminMapper.class);

    // Entity → DTO 매핑
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "ratingAvg", target = "ratingAvg")
    AdminDTO toDto(User user);

    // DTO → Entity 업데이트 매핑 (새로 추가)
    @Mapping(target = "gender", expression = "java(User.Gender.valueOf(dto.getGender()))")
    void updateFromDto(AdminUpdateUserDTO dto, @MappingTarget User user);
}
