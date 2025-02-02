package com.example.festimo.domain.follow.dto

import java.time.LocalDateTime

data class FollowResponseDTO(
    val id: Long,
    val followerId: Long,
    val followeeId: Long,
    val createdAt: LocalDateTime
)