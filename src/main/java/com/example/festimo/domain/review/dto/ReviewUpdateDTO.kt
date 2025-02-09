package com.example.festimo.domain.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class ReviewUpdateDTO(
    @field:Min(1)
    @field:Max(5)
    val rating: Int, // 평점

    @field:NotBlank
    val content: String // 수정할 내용
)


