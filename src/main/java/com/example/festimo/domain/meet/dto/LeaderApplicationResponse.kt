package com.example.festimo.domain.meet.dto

import com.example.festimo.domain.meet.entity.Applications
import java.time.LocalDateTime

data class LeaderApplicationResponse(
    val applicationId: Long,         // 신청 ID
    val userId: Long,               // 신청한 사용자 ID
    val companionId: Long,          // 동행 ID
    val nickname: String,           // 신청한 사용자의 닉네임
    val status: Applications.Status, // 신청 상태 (PENDING, ACCEPTED, REJECTED)
    val appliedDate: LocalDateTime  // 신청 날짜
)