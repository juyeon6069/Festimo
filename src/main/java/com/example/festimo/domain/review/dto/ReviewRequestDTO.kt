package com.example.festimo.domain.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ReviewRequestDTO(
    val reviewerId: Long? = null,
    val revieweeId: Long,
    val content: String,
    val applicationId: Long? = null, // nullable로 변경
    val companyId2: Long? = null, // nullable로 변경

    @field:Min(1)
    @field:Max(5) // 평점은 1~5 사이의 정수만 허용
    val rating: Int
)

