package com.example.festimo.domain.review.dto

import java.time.LocalDateTime

data class ReviewResponseDTO(
    val reviewId: Long,
    val reviewerId: Long,
    val revieweeId: Long,
    val rating: Int,
    val content: String,
    val createdAt: LocalDateTime
)
