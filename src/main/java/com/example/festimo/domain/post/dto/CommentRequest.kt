package com.example.festimo.domain.post.dto

import jakarta.validation.constraints.NotBlank

data class CommentRequest(
    @field:NotBlank(message = "댓글은 필수 입력 항목입니다.")
    val comment: String
)