package com.example.festimo.domain.meet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;

import com.example.festimo.domain.meet.dto.LeaderApplicationResponse;
import com.example.festimo.domain.meet.entity.Applications;


@Mapper
public interface LeaderApplicationMapper {

    LeaderApplicationMapper INSTANCE = Mappers.getMapper(LeaderApplicationMapper.class);

    // Entity → DTO 매핑
    @Mapping(source = "application.applicationId", target = "applicationId")
    @Mapping(source = "application.userId", target = "userId")
    @Mapping(source = "application.companionId", target = "companionId")
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(source = "application.status", target = "status")
    @Mapping(source = "application.appliedDate", target = "appliedDate")
    LeaderApplicationResponse toDto(Applications application, String nickname);

}