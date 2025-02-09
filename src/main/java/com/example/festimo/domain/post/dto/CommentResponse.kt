package com.example.festimo.domain.post.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CommentResponse(
    val userId: Long?,
    val sequence: Int,
    val comment: String,
    val nickname: String,
    val postId: Long,
    var owner: Boolean = false,
    var admin: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,

    @JsonIgnore
    val id: Long? = null
)