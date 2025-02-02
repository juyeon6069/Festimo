package com.example.festimo.domain.meet.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import com.example.festimo.domain.meet.dto.LeaderApplicationResponse
import com.example.festimo.domain.meet.dto.ApplicateUsersProjection

@Mapper
interface LeaderApplicationMapper {
    companion object {
        val INSTANCE: LeaderApplicationMapper = Mappers.getMapper(LeaderApplicationMapper::class.java)
    }

    fun toDto(projection: ApplicateUsersProjection): LeaderApplicationResponse

    fun toDtoList(projections: List<ApplicateUsersProjection>): List<LeaderApplicationResponse>
}