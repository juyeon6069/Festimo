package com.example.festimo.domain.meet.dto

import com.example.festimo.domain.meet.entity.Applications
import java.time.LocalDateTime

data class ApplicationResponse(
    val applicationId: Long,
    val userId: Long,
    val companionId: Long,
    val status: Applications.Status,
    val appliedDate: LocalDateTime
)