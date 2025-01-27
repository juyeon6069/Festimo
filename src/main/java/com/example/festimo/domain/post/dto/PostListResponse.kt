package com.example.festimo.domain.post.dto

import com.example.festimo.domain.post.entity.Post
import com.example.festimo.domain.post.entity.PostCategory
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PostListResponse(
    val id: Long? = null,
    val nickname: String = "",
    val avatar: String = "",
    val time: String = "",
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val replies: Int = 0,
    val views: Int = 0,
    val category: PostCategory? = null
) {
    constructor(post: Post) : this(
        id = post.id,
        nickname = post.user?.nickname ?: "",
        avatar = when {
            !post.user?.avatar.isNullOrEmpty() -> "/imgs/${post.user?.avatar}"
            else -> "/imgs/default-avatar.png"
        },
        time = post.createdAt?.let { calculateTime(it) } ?: "Unknown time",
        title = post.title,
        content = post.content,
        tags = post.tags.toList(),
        replies = post.comments.size,
        views = post.views,
        category = post.category
    )

    companion object {
        private fun calculateTime(createdAt: LocalDateTime): String {
            val now = LocalDateTime.now()
            val duration = Duration.between(createdAt, now)

            return when {
                duration.toMinutes() < 60 -> "${duration.toMinutes()} ${if (duration.toMinutes() == 1L) "min" else "mins"} ago"
                duration.toHours() < 24 -> "${duration.toHours()} ${if (duration.toHours() == 1L) "hour" else "hours"} ago"
                duration.toDays() <= 7 -> "${duration.toDays()} ${if (duration.toDays() == 1L) "day" else "days"} ago"
                else -> createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
        }
    }
}