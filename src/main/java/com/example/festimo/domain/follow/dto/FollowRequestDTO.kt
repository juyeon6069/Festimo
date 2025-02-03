package com.example.festimo.domain.follow.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// 팔로우 요청 시 필요한 데이터
data class FollowRequestDTO @JsonCreator constructor(
    @JsonProperty("followerId") val followerId: Long,
    @JsonProperty("followeeId") val followeeId: Long
)