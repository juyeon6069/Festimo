package com.example.festimo.domain.post.dto

import com.example.festimo.domain.post.entity.PostCategory
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PostDetailResponse(
    var id: Long? = null,
    var postId: Long? = null,
    var title: String = "",
    var nickname: String = "",
    var avatar: String? = null,
    var mail: String? = null,
    var content: String = "",
    var category: PostCategory? = null,
    var views: Int = 0,
    var replies: Int = 0,
    var likes: Int = 0,
    var tags: Set<String> = emptySet(),
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var owner: Boolean = false,
    var admin: Boolean = false,
    var liked: Boolean = false,
    var comments: List<CommentResponse> = ArrayList()
)