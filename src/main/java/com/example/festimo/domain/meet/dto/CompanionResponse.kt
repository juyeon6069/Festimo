package com.example.festimo.domain.meet.dto

import com.example.festimo.domain.meet.entity.MemberResponse

data class CompanionResponse(
    val companionId: Long,  // 모임 id
    val leaderId: Long,    // 리더 id
    val leaderName: String,  // 리더 이름
    val members: List<MemberResponse>  // 동행원들
)
