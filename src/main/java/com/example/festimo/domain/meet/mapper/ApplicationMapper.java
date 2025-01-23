package com.example.festimo.domain.meet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.example.festimo.domain.meet.dto.ApplicationResponse;
import com.example.festimo.domain.meet.entity.Applications;

@Mapper
public interface ApplicationMapper {


    ApplicationMapper INSTANCE= Mappers.getMapper(ApplicationMapper.class);

    // Entity → DTO 매핑
    @Mapping(source = "applicationId", target = "applicationId")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "companionId", target = "companionId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "appliedDate", target = "appliedDate")
    ApplicationResponse toDto(Applications application);

}


